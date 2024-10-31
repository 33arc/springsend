package com.github.arc33.springsend.service.user;

import com.github.arc33.springsend.domain.event.RegistrationStatus;
import com.github.arc33.springsend.domain.event.UserRegistrationEvent;
import com.github.arc33.springsend.dto.user.UserRegisterRequest;
import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.model.CognitoUser;
import com.github.arc33.springsend.model.User;
import com.github.arc33.springsend.repository.UserRegistrationEventRepository;
import com.google.protobuf.Api;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Validated
@Slf4j
public class CognitoUserManagementService implements UserManagementService {
    private final CognitoIdentityProviderClient cognitoClient;

    private final CacheManager cacheManager;

    private final UserRegistrationEventRepository registrationEventRepository;

    @Value("${aws.cognito.userPoolId}")
    private final String userPoolId;

    @Value("${aws.cognito.clientId}")
    private final String clientId;

    @Value("${user-management-service.max-page-size}")
    private Integer MAX_PAGE_SIZE;

    @Override
    public Page<User> searchUsers(String searchTerm, int page, int size) {
        validatePaginationParams(page, size);

        try {
            ListUsersRequest request = buildUserSearchRequest(searchTerm, page, size);
            ListUsersResponse response = cognitoClient.listUsers(request);

            List<User> users = response.users().stream()
                    .map(this::mapToCognitoUser)
                    .collect(Collectors.toList());

            long totalUsers = getTotalUsers(searchTerm);
            return new PageImpl<>(users, PageRequest.of(page, size), totalUsers);
        } catch (CognitoIdentityProviderException e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public void registerUser(@Valid UserRegisterRequest request) {
        log.info("Starting user registration for username: {}", request.getUsername());

        // Create registration event
        UserRegistrationEvent event = UserRegistrationEvent.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .role(request.getRole())
                .status(RegistrationStatus.PENDING)
                .timestamp(Instant.now())
                .build();

        registrationEventRepository.save(event);

        try {
            // Step 1: Sign up user in Cognito
            String userSub = signUpUserInCognito(request);
            event.setUserSub(userSub);
            event.setStatus(RegistrationStatus.USER_CREATED);
            registrationEventRepository.save(event);

            // Step 2: Add user to group
            addUserToGroup(userSub, request.getUsername(), request.getRole());
            event.setStatus(RegistrationStatus.COMPLETED);
            registrationEventRepository.save(event);

            log.info("Successfully registered user: {}", request.getUsername());

        } catch (UsernameExistsException e) {
            event.setStatus(RegistrationStatus.FAILED);
            event.setErrorMessage("User already exists");
            registrationEventRepository.save(event);

            log.error("Username already exists: {}", request.getUsername());
            throw ApiErrorType.RESOURCE_ALREADY_EXISTS.toException("User already exists");

        } catch (Exception e) {
            event.setStatus(RegistrationStatus.FAILED);
            event.setErrorMessage(e.getMessage());
            registrationEventRepository.save(event);

            log.error("Registration failed for user: {}", request.getUsername(), e);

            // Schedule async cleanup
            scheduleCompensatingTransaction(request.getUsername());

            throw ApiErrorType.INTERNAL_ERROR.toException("Registration failed");
        }
    }

    @Async
    protected void scheduleCompensatingTransaction(String username) {
        compensatingTransaction(username);
    }

    @Retryable(
            value = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    protected void compensatingTransaction(String username) {
        try {
            AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();
            cognitoClient.adminDeleteUser(deleteRequest);
            log.info("Successfully executed compensating transaction for user: {}", username);

        } catch (Exception e) {
            log.error("Failed to execute compensating transaction for user: {}. Manual intervention required",
                    username, e);
            notifyOperationsTeam(username, e);
        }
    }

    private void addUserToGroup(String userSub, String username, String role) {
        AdminAddUserToGroupRequest groupRequest = AdminAddUserToGroupRequest.builder()
                .userPoolId(userPoolId)
                .username(username)
                .groupName(role)
                .build();

        cognitoClient.adminAddUserToGroup(groupRequest);
    }

    private void notifyOperationsTeam(String username, Exception e) {
        // Implement notification logic (e.g., email, Slack, etc.)
        log.error("Registration cleanup failed for user: {}. Manual cleanup required.", username, e);
        // Add actual notification implementation
    }


    @Override
    public void activateUser(String username, String confirmationCode) {
        validateActivationParams(username, confirmationCode);

        try {
            ConfirmSignUpRequest request = ConfirmSignUpRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .confirmationCode(confirmationCode)
                    .build();

            cognitoClient.confirmSignUp(request);
            log.info("Successfully activated user: {}", username);

        } catch (CodeMismatchException e) {
            log.error("Invalid confirmation code for user: {}", username);
            throw ApiErrorType.INVALID_PARAMETER.toException("Invalid confirmation code provided");
        } catch (ExpiredCodeException e) {
            log.error("Expired confirmation code for user: {}", username);
            throw ApiErrorType.INVALID_PARAMETER.toException("Expired confirmation code provided");
        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to activate user: {}", username, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to activate user");
        }
    }

    @Override
    public void resendConfirmationCode(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        try {
            ResendConfirmationCodeRequest request = ResendConfirmationCodeRequest.builder()
                    .clientId(clientId)
                    .username(username)
                    .build();

            cognitoClient.resendConfirmationCode(request);
            log.info("Successfully resent confirmation code to user: {}", username);

        } catch (LimitExceededException e) {
            log.error("Code resend limit exceeded for user: {}", username);
            throw ApiErrorType.RATE_LIMIT_EXCEEDED.toException("Code resend limit exceeded");
        } catch (UserNotFoundException e) {
            log.error("User not found: {}", username);
            throw ApiErrorType.RESOURCE_NOT_FOUND.toException("User not found");
        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to resend confirmation code: {}", username, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to resend confirmation code");
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void adminConfirmSignUp(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }

        try {
            AdminConfirmSignUpRequest request = AdminConfirmSignUpRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            cognitoClient.adminConfirmSignUp(request);
            log.info("Admin confirmed signup for user: {}", username);

        } catch (UserNotFoundException e) {
            log.error("User not found during admin confirmation: {}", username);
            throw ApiErrorType.RESOURCE_NOT_FOUND.toException("User does not exist");
        } catch (CognitoIdentityProviderException e) {
            log.error("Admin confirmation failed for user: {}", username, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to confirm user signup");
        }
    }

    @Cacheable(value = "userRole",
            key = "#username",
            unless = "#result == null")
    @Override
    public String getRole(String username) {
        validateUsername(username);

        try {
            AdminGetUserRequest request = AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            AdminGetUserResponse response = cognitoClient.adminGetUser(request);
            String role = extractRole(response.userAttributes());

            if (role == null) {
                log.warn("No role found for user: {}", username);
                throw ApiErrorType.RESOURCE_NOT_FOUND.toException("No role found for user");
            }

            return role;

        } catch (UserNotFoundException e) {
            log.error("User not found: {}", username);
            throw ApiErrorType.RESOURCE_NOT_FOUND.toException("User not found");
        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to get role for user: {}", username, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to get role");
        }
    }

    @CacheEvict(value = {"userRoles", "userDetails"}, key = "#username")
    @PreAuthorize("hasRole('ADMIN')")
    @Override
    public void deleteUser(String username) {
        validateUsername(username);

        try {
            AdminDeleteUserRequest request = AdminDeleteUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(username)
                    .build();

            cognitoClient.adminDeleteUser(request);
            evictCaches(username);
            log.info("Successfully deleted user: {}", username);

        } catch (UserNotFoundException e) {
            log.error("User not found during deletion: {}", username);
            throw ApiErrorType.RESOURCE_NOT_FOUND.toException("User not found");
        } catch (NotAuthorizedException e) {
            log.error("Not authorized to delete user: {}", username);
            throw ApiErrorType.NOT_AUTHORIZED.toException("Not authorized to delete user");
        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to delete user: {}", username, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to delete user");
        }
    }

@Cacheable(value = "users",
        key = "'allUsers-' + #pageable.pageNumber + '-' + #pageable.pageSize",
        unless = "#result == null")
    @Override
    public Page<User> listUsers(int page, int size) {
        validatePaginationParams(page, size);

        try {
            ListUsersRequest request = buildListUsersRequest(page, size);
            ListUsersResponse response = cognitoClient.listUsers(request);

            List<User> users = response.users().stream()
                    .map(this::mapToCognitoUser)
                    .collect(Collectors.toList());

            long totalUsers = getCachedTotalUsers();
            return new PageImpl<>(users, PageRequest.of(page, size), totalUsers);

        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to list users. Page: {}, Size: {}", page, size, e);
            throw ApiErrorType.INTERNAL_ERROR.toException("Failed to list users");
        }
    }

    private ListUsersRequest buildListUsersRequest(int page, int size) {
        ListUsersRequest.Builder builder = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .limit(size);

        if (page > 0) {
            String token = getPaginationToken(page, size, null);
            builder.paginationToken(token);
        }

        return builder.build();
    }

    @Cacheable(value = "totalUsers", unless = "#result == 0")
    public long getCachedTotalUsers() {
        return getTotalUsers(null);
    }

    private String extractRole(List<AttributeType> attributes) {
        return attributes.stream()
                .filter(attr -> attr.name().equals("custom:role"))
                .findFirst()
                .map(AttributeType::value)
                .orElse(null);
    }

    private void validateUsername(String username) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
    }

    private void validateActivationParams(String username, String confirmationCode) {
        if (StringUtils.isEmpty(username)) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (StringUtils.isEmpty(confirmationCode)) {
            throw new IllegalArgumentException("Confirmation code cannot be empty");
        }
    }

    private String signUpUserInCognito(UserRegisterRequest request) {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .clientId(clientId)
                .username(request.getUsername())
                .password(request.getPassword())
                .userAttributes(buildUserAttributes(request))
                .build();

        SignUpResponse response = cognitoClient.signUp(signUpRequest);
        return response.userSub();
    }

    private List<AttributeType> buildUserAttributes(UserRegisterRequest request) {
        return Arrays.asList(
                AttributeType.builder()
                        .name("email")
                        .value(request.getEmail())
                        .build(),
                AttributeType.builder()
                        .name("email_verified")
                        .value("true")
                        .build(),
                AttributeType.builder()
                        .name("nickname")
                        .value(request.getUsername())
                        .build()
        );
    }

    private void validatePaginationParams(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Page size cannot exceed " + MAX_PAGE_SIZE);
        }
    }

    private ListUsersRequest buildUserSearchRequest(String searchTerm, int page, int size) {
        ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .limit(size);

        if (StringUtils.hasText(searchTerm)) {
            requestBuilder.filter(buildSearchFilter(searchTerm));
        }

        if (page > 0) {
            String paginationToken = getPaginationToken(page, size, searchTerm);
            requestBuilder.paginationToken(paginationToken);
        }

        return requestBuilder.build();
    }

    private String buildSearchFilter(String searchTerm) {
        String sanitizedTerm = sanitizeSearchTerm(searchTerm);
        return String.format("username ^= \"%s\" or email ^= \"%s\"",
                sanitizedTerm, sanitizedTerm);
    }

    private String sanitizeSearchTerm(String searchTerm) {
        return searchTerm.replaceAll("[\"\\\\]", "");
    }

    private String getPaginationToken(int page, int size, String searchTerm) {
        ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder()
                .userPoolId(userPoolId)
                .limit(size);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String filter = String.format("username ^= \"%s\" or email ^= \"%s\"",
                    searchTerm, searchTerm);
            requestBuilder.filter(filter);
        }

        String paginationToken = null;
        for (int i = 0; i < page; i++) {
            ListUsersResponse response = cognitoClient.listUsers(
                    requestBuilder.paginationToken(paginationToken).build());
            paginationToken = response.paginationToken();
            if (paginationToken == null) {
                break; // No more pages
            }
        }
        return paginationToken;
    }

    private long getTotalUsers(String searchTerm) {
        ListUsersRequest.Builder requestBuilder = ListUsersRequest.builder()
                .userPoolId(userPoolId);

        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String filter = String.format("username ^= \"%s\" or email ^= \"%s\"",
                    searchTerm, searchTerm);
            requestBuilder.filter(filter);
        }

        ListUsersResponse response = cognitoClient.listUsers(requestBuilder.build());
        return response.users().size(); // This is an approximation
    }

    private CognitoUser mapToCognitoUser(UserType user) {
        String email = user.attributes().stream()
                .filter(attr -> attr.name().equals("email"))
                .findFirst()
                .map(AttributeType::value)
                .orElse(null);

        String role = user.attributes().stream()
                .filter(attr -> attr.name().equals("custom:role"))
                .findFirst()
                .map(AttributeType::value)
                .orElse("USER"); // Default role if not set

        return new CognitoUser(user.username(), email, role, user.userStatus().toString());
    }

    private void evictCaches(String username) {
        cacheManager.getCache("users").clear();
        cacheManager.getCache("userRole").evict(username);
    }
}

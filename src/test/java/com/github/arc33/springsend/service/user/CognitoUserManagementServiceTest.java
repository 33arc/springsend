package com.github.arc33.springsend.service.user;

import com.github.arc33.springsend.dto.user.UserRegisterRequest;
import com.github.arc33.springsend.exception.custom.ApiException;
import com.github.arc33.springsend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CognitoUserManagementServiceTest {
    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @InjectMocks
    private CognitoUserManagementService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "userPoolId", "test-pool");
        ReflectionTestUtils.setField(userService, "clientId", "test-client");
        ReflectionTestUtils.setField(userService, "MAX_PAGE_SIZE", 50);
    }

    @Nested
    class SearchUsersTests {
        @Test
        void searchUsers_ValidParameters_ReturnsUserPage() {
            // Arrange
            String searchTerm = "test";
            ListUsersResponse mockResponse = ListUsersResponse.builder()
                    .users(Collections.singletonList(createMockUserType("testuser")))
                    .build();
            when(cognitoClient.listUsers(any(ListUsersRequest.class))).thenReturn(mockResponse);

            // Act
            Page<User> result = userService.searchUsers(searchTerm, 0, 10);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            // Verify listUsers was called exactly twice (search and count)
            verify(cognitoClient, times(2)).listUsers(any(ListUsersRequest.class));
        }

        @Test
        void searchUsers_InvalidPageSize_ThrowsException() {
            assertThrows(IllegalArgumentException.class,
                    () -> userService.searchUsers("test", 0, 51));
        }
    }

    @Nested
    class RegisterUserTests {
        @Test
        void registerUser_ValidRequest_SuccessfullyRegisters() {
            // Arrange
            UserRegisterRequest request = new UserRegisterRequest("testuser", "test@email.com", "password", "USER");
            SignUpResponse signUpResponse = SignUpResponse.builder().userSub("test-sub").build();
            when(cognitoClient.signUp(any(SignUpRequest.class))).thenReturn(signUpResponse);
            when(cognitoClient.adminAddUserToGroup(any(AdminAddUserToGroupRequest.class))).thenReturn(any(AdminAddUserToGroupResponse.class));

            // Act & Assert
            assertDoesNotThrow(() -> userService.registerUser(request));
            verify(cognitoClient).signUp(any(SignUpRequest.class));
            verify(cognitoClient).adminAddUserToGroup(any(AdminAddUserToGroupRequest.class));
        }

        @Test
        void registerUser_UsernameExists_ThrowsException() {
            UserRegisterRequest request = new UserRegisterRequest("existinguser", "test@email.com", "password", "USER");
            when(cognitoClient.signUp(any(SignUpRequest.class)))
                    .thenThrow(UsernameExistsException.class);

            ApiException exception = assertThrows(ApiException.class,
                    () -> userService.registerUser(request));
            assertTrue(exception.getErrorResponse().getDescription().contains("User already exists"));
        }
    }

    @Nested
    class ActivateUserTests {
        @Test
        void activateUser_ValidCode_SuccessfullyActivates() {
            when(cognitoClient.confirmSignUp(any(ConfirmSignUpRequest.class))).thenReturn(any(ConfirmSignUpResponse.class));

            assertDoesNotThrow(() -> userService.activateUser("testuser", "123456"));
            verify(cognitoClient).confirmSignUp(any(ConfirmSignUpRequest.class));
        }

        @Test
        void activateUser_InvalidCode_ThrowsException() {
            when(cognitoClient.confirmSignUp(any(ConfirmSignUpRequest.class)))
                    .thenThrow(CodeMismatchException.class);

            assertThrows(ApiException.class,
                    () -> userService.activateUser("testuser", "invalid"));
        }
    }

    @Nested
    class GetRoleTests {
        @Test
        void getRole_ExistingUser_ReturnsRole() {
            AdminGetUserResponse mockResponse = AdminGetUserResponse.builder()
                    .userAttributes(Arrays.asList(
                            AttributeType.builder().name("custom:role").value("ADMIN").build()
                    ))
                    .build();
            when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
                    .thenReturn(mockResponse);

            String role = userService.getRole("testuser");
            assertEquals("ADMIN", role);
        }

        @Test
        void getRole_UserNotFound_ThrowsException() {
            when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
                    .thenThrow(UserNotFoundException.class);

            assertThrows(ApiException.class, () -> userService.getRole("nonexistent"));
        }
    }

    private UserType createMockUserType(String username) {
        return UserType.builder()
                .username(username)
                .attributes(Arrays.asList(
                        AttributeType.builder().name("email").value("test@email.com").build(),
                        AttributeType.builder().name("custom:role").value("USER").build()
                ))
                .userStatus("CONFIRMED")
                .build();
    }
}

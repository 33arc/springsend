package com.github.arc33.springsend.controller;

import com.github.arc33.springsend.dto.user.UserRegisterRequest;
import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.exception.custom.ApiException;
import com.github.arc33.springsend.model.User;
import com.github.arc33.springsend.service.user.UserManagementService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('UPLOADER')")
    public ResponseEntity<Page<User>> findAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userManagementService.listUsers(page,size));
    }

    @DeleteMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> deleteUser(
            @PathVariable String username,
            Authentication authentication){

        if (!isAuthorizedToDeleteUser(username, authentication)) {
            throw ApiErrorType.NOT_AUTHORIZED
                    .toException("Only admins can delete other admins");
        }

        userManagementService.deleteUser(username);
        return ResponseEntity.ok("User deleted successfully");
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<String> register(
            @Valid @RequestBody UserRegisterRequest request) {
        userManagementService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("User registered successfully");
    }

    // Helper methods
    private boolean isAuthorizedToDeleteUser(String username, Authentication authentication) {
        String userRole = userManagementService.getRole(username);
        boolean isTargetUserAdmin = "ADMIN".equals(userRole);
        boolean isCurrentUserAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        return !isTargetUserAdmin || isCurrentUserAdmin;
    }

}

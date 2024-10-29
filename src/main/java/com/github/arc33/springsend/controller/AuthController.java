package com.github.arc33.springsend.controller;

import com.github.arc33.springsend.config.SecurityConfig;
import com.github.arc33.springsend.dto.password.ConfirmResetPasswordRequest;
import com.github.arc33.springsend.dto.password.ResetPasswordRequest;
import com.github.arc33.springsend.dto.token.TokenRefreshRequest;
import com.github.arc33.springsend.dto.token.TokenRefreshResponse;
import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;
import com.github.arc33.springsend.exception.custom.ApiErrorType;
import com.github.arc33.springsend.service.auth.AuthenticationService;
import com.github.arc33.springsend.service.auth.PasswordService;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@AllArgsConstructor
public class AuthController {
    private final AuthenticationService authenticationService;
    private final PasswordService passwordService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request){
        UserLoginResponse response = null;
        try {
            response = authenticationService.loginUser(request);
        } catch(Exception e){
            throw ApiErrorType
                    .INVALID_CREDENTIALS
                    .toException();
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        String token = extractAndValidateToken(authorizationHeader);

        try {
            authenticationService.logout(token);
            return ResponseEntity.ok("Logged out successfully");
        } catch (Exception e) {
            throw ApiErrorType.INTERNAL_ERROR.toException("Logout operation failed");
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request){
        TokenRefreshResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/users/{username}/verification")
    public ResponseEntity<String> verify(
            @PathVariable String username,
            @RequestParam String verificationCode) {
        authenticationService.activateUser(username, verificationCode);
        return ResponseEntity.ok("User activated successfully");
    }

    @PostMapping("/users/{username}/confirmation-code")
    public ResponseEntity<String> resendConfirmationCode(@PathVariable String username) {
        authenticationService.resendConfirmationCode(username);
        return ResponseEntity.ok("Confirmation code resent successfully");
    }

    @PostMapping("/password-reset-requests")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ResetPasswordRequest requestDto) {
        passwordService.passwordResetRequest(requestDto);
        return ResponseEntity.ok("Password reset request initiated. Please check your email.");
    }

    @PostMapping("/password-reset-confirmations")
    public ResponseEntity<String> confirmResetPassword(@Valid @RequestBody ConfirmResetPasswordRequest requestDto) {
        passwordService.confirmResetPassword(requestDto);
        return ResponseEntity.ok("Password reset successfully.");
    }

    private String extractAndValidateToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw ApiErrorType.NOT_AUTHORIZED.toException("Invalid Authorization header");
        }
        return authorizationHeader.substring(7);
    }

}

package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.dto.password.ConfirmResetPasswordRequest;
import com.github.arc33.springsend.dto.password.ResetPasswordRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CognitoPasswordServiceTest {
    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @InjectMocks
    private CognitoPasswordService passwordService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordService, "clientId", "test-client-id");
    }

    @Test
    void passwordResetRequest_ShouldCallCognitoClient() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("testUser");
        ArgumentCaptor<ForgotPasswordRequest> requestCaptor = ArgumentCaptor.forClass(ForgotPasswordRequest.class);

        // Act
        passwordService.passwordResetRequest(request);

        // Assert
        verify(cognitoClient).forgotPassword(requestCaptor.capture());
        ForgotPasswordRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-client-id", capturedRequest.clientId());
        assertEquals("testUser", capturedRequest.username());
    }

    @Test
    void confirmResetPassword_ShouldCallCognitoClient() {
        // Arrange
        ConfirmResetPasswordRequest request = new ConfirmResetPasswordRequest("testUser", "123456", "newPass");
        ArgumentCaptor<ConfirmForgotPasswordRequest> requestCaptor =
                ArgumentCaptor.forClass(ConfirmForgotPasswordRequest.class);

        // Act
        passwordService.confirmResetPassword(request);

        // Assert
        verify(cognitoClient).confirmForgotPassword(requestCaptor.capture());
        ConfirmForgotPasswordRequest capturedRequest = requestCaptor.getValue();
        assertEquals("test-client-id", capturedRequest.clientId());
        assertEquals("testUser", capturedRequest.username());
        assertEquals("123456", capturedRequest.confirmationCode());
        assertEquals("newPass", capturedRequest.password());
    }

    @Test
    void passwordResetRequest_WhenCognitoFails_ShouldPropagateException() {
        // Arrange
        ResetPasswordRequest request = new ResetPasswordRequest("testUser");
        doThrow(new RuntimeException("Cognito error")).when(cognitoClient).forgotPassword((ForgotPasswordRequest) any());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> passwordService.passwordResetRequest(request));
    }
}

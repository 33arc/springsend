package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.SpringsendApplication;
import com.github.arc33.springsend.dto.password.ConfirmResetPasswordRequest;
import com.github.arc33.springsend.dto.password.ResetPasswordRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

// Integration Tests
@SpringBootTest(classes = {
        SpringsendApplication.class,
        BaseIntegrationTest.CommonTestConfig.class
})
@Testcontainers
class CognitoPasswordServiceIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private CognitoPasswordService passwordService;

    @MockBean(name = "cognitoClient")
    private CognitoIdentityProviderClient cognitoClient;

    @Test
    void passwordResetFlow_ShouldSucceed() {
        // Arrange
        ResetPasswordRequest resetRequest = new ResetPasswordRequest("testUser");
        ConfirmResetPasswordRequest confirmRequest =
                new ConfirmResetPasswordRequest("testUser", "123456", "newPass");

        // Act & Assert
        assertDoesNotThrow(() -> passwordService.passwordResetRequest(resetRequest));
        assertDoesNotThrow(() -> passwordService.confirmResetPassword(confirmRequest));

        verify(cognitoClient).forgotPassword((ForgotPasswordRequest) any());
        verify(cognitoClient).confirmForgotPassword((ConfirmForgotPasswordRequest) any());
    }

    @Test
    void passwordResetFlow_WithInvalidCode_ShouldFail() {
        // Arrange
        ConfirmResetPasswordRequest confirmRequest =
                new ConfirmResetPasswordRequest("testUser", "invalid", "newPass");

        software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException cognitoException =
                software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException.builder()
                        .message("Invalid verification code provided")
                        .build();

        doThrow(cognitoException)
                .when(cognitoClient).confirmForgotPassword((ConfirmForgotPasswordRequest) any());

        // Act & Assert
        assertThrows(software.amazon.awssdk.services.cognitoidentityprovider.model.CodeMismatchException.class,
                () -> passwordService.confirmResetPassword(confirmRequest));
    }
}

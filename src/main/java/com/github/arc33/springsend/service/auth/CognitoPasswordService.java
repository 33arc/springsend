package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.dto.password.ConfirmResetPasswordRequest;
import com.github.arc33.springsend.dto.password.ResetPasswordRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmForgotPasswordRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ForgotPasswordRequest;

@Service
@RequiredArgsConstructor
public class CognitoPasswordService implements PasswordService {
    private final CognitoIdentityProviderClient cognitoClient;
    @Value("${aws.cognito.clientId}")
    private final String clientId;

    @Override
    public void passwordResetRequest(ResetPasswordRequest userRequest) {
        ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                .clientId(clientId)
                .username(userRequest.getUsername())
                .build();
        cognitoClient.forgotPassword(request);
    }

    @Override
    public void confirmResetPassword(ConfirmResetPasswordRequest userRequest) {
        ConfirmForgotPasswordRequest confirmForgotPasswordRequest = ConfirmForgotPasswordRequest.builder()
                .clientId(clientId)
                .username(userRequest.getUsername())
                .confirmationCode(userRequest.getCode())
                .password(userRequest.getNewPassword())
                .build();
        cognitoClient.confirmForgotPassword(confirmForgotPasswordRequest);
    }
}
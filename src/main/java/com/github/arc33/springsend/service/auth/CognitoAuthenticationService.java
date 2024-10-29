package com.github.arc33.springsend.service.auth;


import com.github.arc33.springsend.config.SecurityConfig;
import com.github.arc33.springsend.dto.token.TokenRefreshRequest;
import com.github.arc33.springsend.dto.token.TokenRefreshResponse;
import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CognitoAuthenticationService implements AuthenticationService {
    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfig.class);
    private final CognitoIdentityProviderClient cognitoClient;
    @Value("${aws.cognito.clientId}")
    private final String clientId;
    private final TokenBlacklistService tokenBlacklistService;

    public UserLoginResponse loginUser(UserLoginRequest loginRequest) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", loginRequest.getUsername());
        authParams.put("PASSWORD", loginRequest.getPassword());

        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        InitiateAuthResponse initiateAuthResult;
        try {
            initiateAuthResult = cognitoClient.initiateAuth(initiateAuthRequest);
        }catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw e;
        }
        AuthenticationResultType authenticationResult = initiateAuthResult.authenticationResult();

        return new UserLoginResponse(
                authenticationResult.idToken(),
                authenticationResult.accessToken(),
                authenticationResult.refreshToken()
        );
    }

    public void activateUser(String username, String confirmationCode) {
        ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
                .clientId(clientId)
                .username(username)
                .confirmationCode(confirmationCode)
                .build();

        cognitoClient.confirmSignUp(confirmSignUpRequest);
    }

    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        Map<String, String> authParams = new HashMap<>();
        authParams.put("REFRESH_TOKEN", request.getToken());

        InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        InitiateAuthResponse initiateAuthResult = cognitoClient.initiateAuth(initiateAuthRequest);
        AuthenticationResultType authenticationResult = initiateAuthResult.authenticationResult();

        return new TokenRefreshResponse(
                authenticationResult.accessToken(),
                "Bearer",
                authenticationResult.expiresIn(),
                authenticationResult.refreshToken()
        );
    }

    @Override
    public void logout(String accessToken){
        GlobalSignOutRequest globalSignOutRequest = GlobalSignOutRequest.builder()
                .accessToken(accessToken)
                .build();
        try {
            cognitoClient.globalSignOut(globalSignOutRequest);
        } catch (Exception e) {
            LOG.error(e.getMessage(),e);
            throw e;
        }
        tokenBlacklistService.addToBlacklist(accessToken);
    }

    @Override
    public void resendConfirmationCode(String username) {
        ResendConfirmationCodeRequest resendConfirmationCodeRequest = ResendConfirmationCodeRequest.builder()
                .clientId(clientId)
                .username(username)
                .build();

        cognitoClient.resendConfirmationCode(resendConfirmationCodeRequest);
    }
}
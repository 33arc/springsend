package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.SpringsendApplication;
import com.github.arc33.springsend.config.SecurityConfig;
import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;
import com.github.arc33.springsend.filter.JwtTokenFilter;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.CockroachContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {
        SpringsendApplication.class,
        BaseIntegrationTest.CommonTestConfig.class
})
@Testcontainers
class CognitoAuthenticationServiceIntegrationTest extends BaseIntegrationTest {
    @MockBean(name = "cognitoClient")
    private CognitoIdentityProviderClient cognitoClient;

    @Autowired
    private CognitoAuthenticationService authService;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @Test
    void fullAuthenticationFlow() {
        // Setup test data
        String username = "testuser";
        String password = "password123";
        String confirmationCode = "123456";
        String accessToken = "access-token";
        String idToken = "id-token";
        String refreshToken = "refresh-token";

        // Mock login response
        InitiateAuthResponse loginResponse = InitiateAuthResponse.builder()
                .authenticationResult(AuthenticationResultType.builder()
                        .accessToken(accessToken)
                        .idToken(idToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
        when(cognitoClient.initiateAuth((InitiateAuthRequest) any())).thenReturn(loginResponse);

        // Test activation
        authService.activateUser(username, confirmationCode);
        verify(cognitoClient).confirmSignUp(any(ConfirmSignUpRequest.class));

        // Test login
        UserLoginResponse loginResult = authService.loginUser(
                new UserLoginRequest(username, password)
        );
        assertEquals(accessToken, loginResult.getAccessToken());
        assertEquals(idToken, loginResult.getIdToken());
        assertEquals(refreshToken, loginResult.getRefreshToken());

        // Test logout
        authService.logout(accessToken);
        verify(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));
        verify(tokenBlacklistService).addToBlacklist(accessToken);
    }
}
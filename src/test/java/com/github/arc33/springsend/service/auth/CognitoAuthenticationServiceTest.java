package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CognitoAuthenticationServiceTest {
    @Mock
    private CognitoIdentityProviderClient cognitoClient;
    @Mock
    private TokenBlacklistService tokenBlacklistService;
    @InjectMocks
    private CognitoAuthenticationService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "clientId", "test-client-id");
    }

    @Test
    void loginUser_Success() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("username", "password");
        InitiateAuthResponse mockResponse = InitiateAuthResponse.builder()
                .authenticationResult(AuthenticationResultType.builder()
                        .accessToken("access")
                        .idToken("id")
                        .refreshToken("refresh")
                        .build())
                .build();

        ArgumentCaptor<InitiateAuthRequest> requestCaptor = ArgumentCaptor.forClass(InitiateAuthRequest.class);
        when(cognitoClient.initiateAuth(requestCaptor.capture())).thenReturn(mockResponse);

        // Act
        UserLoginResponse response = authService.loginUser(request);

        // Assert
        verify(cognitoClient).initiateAuth(any(InitiateAuthRequest.class));
        InitiateAuthRequest capturedRequest = requestCaptor.getValue();
        assertEquals(AuthFlowType.USER_PASSWORD_AUTH, capturedRequest.authFlow());
        assertEquals("test-client-id", capturedRequest.clientId());
        assertEquals("username", capturedRequest.authParameters().get("USERNAME"));
        assertEquals("password", capturedRequest.authParameters().get("PASSWORD"));

        assertEquals("access", response.getAccessToken());
        assertEquals("id", response.getIdToken());
        assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void logout_Success() {
        // Arrange
        String accessToken = "test-token";

        // Act
        authService.logout(accessToken);

        // Assert
        verify(cognitoClient).globalSignOut(any(GlobalSignOutRequest.class));
        verify(tokenBlacklistService).addToBlacklist(accessToken);
    }
}
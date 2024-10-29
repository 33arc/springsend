package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.dto.token.TokenRefreshRequest;
import com.github.arc33.springsend.dto.token.TokenRefreshResponse;
import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;

public interface AuthenticationService {
    UserLoginResponse loginUser(UserLoginRequest userLoginRequest);
    TokenRefreshResponse refreshToken(TokenRefreshRequest tokenRefreshRequest);
    void activateUser(String username, String code);
    void logout(String accessToken);
    void resendConfirmationCode(String username);
}
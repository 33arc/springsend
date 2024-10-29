package com.github.arc33.springsend.dto.token;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRefreshResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private int expiresIn;
    private String refreshToken;
}
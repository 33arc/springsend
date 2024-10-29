package com.github.arc33.springsend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    private String idToken;
    private String accessToken;
    private String refreshToken;
}
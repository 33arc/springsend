package com.github.arc33.springsend.controller;

import com.github.arc33.springsend.dto.user.UserLoginRequest;
import com.github.arc33.springsend.dto.user.UserLoginResponse;
import com.github.arc33.springsend.service.auth.AuthenticationService;
import com.github.arc33.springsend.service.blacklist.TokenBlacklistService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Validated
@AllArgsConstructor
public class AuthController {
    private final TokenBlacklistService tokenBlacklistService;
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request){
        UserLoginResponse response = null;
        try {
            response = authenticationService.loginUser(request);
        } catch(Exception e){
            // TODO: implement ApiError
//            throw ApiErrorType
//                    .INVALID_CREDENTIALS
//                    .toException();
        }
        return ResponseEntity.ok(response);
    }

}

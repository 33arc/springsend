package com.github.arc33.springsend.service.auth;

import com.github.arc33.springsend.dto.password.ConfirmResetPasswordRequest;
import com.github.arc33.springsend.dto.password.ResetPasswordRequest;

public interface PasswordService {
    void passwordResetRequest(ResetPasswordRequest forgotPasswordRequest);
    void confirmResetPassword(ConfirmResetPasswordRequest confirmForgotPasswordRequest);
}
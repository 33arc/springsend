package com.github.arc33.springsend.dto.password;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmResetPasswordRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Confirmation code is required")
    @Size(min = 6, max = 6, message = "Confirmation code must be 6 characters long")
    private String code;

    @NotBlank(message = "New password is required")
    @Size(min = 8, message = "New password must be at least 8 characters long")
    private String newPassword;
}
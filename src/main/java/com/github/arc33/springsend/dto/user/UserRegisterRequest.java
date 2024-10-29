package com.github.arc33.springsend.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {
    @NotBlank(message = "Username is required")
    @Size(min=3, max=50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message="Email is required")
    @Email(message="Invalid email format")
    private String email;

    @NotBlank(message="Password is required")
    @Size(min=8,message="Password must be at least 8 characters long")
    private String password;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "^(admin|manager|uploader|client)$", message = "Role must be either 'admin', 'manager', or 'uploader'")
    private String role;
}
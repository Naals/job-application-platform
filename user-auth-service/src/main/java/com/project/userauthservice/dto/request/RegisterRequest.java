package com.project.userauthservice.dto.request;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email(message = "Invalid email format")
        String email,

        @NotBlank @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*]).{8,}$",
                message = "Password must contain uppercase, digit and special character")
        String password,

        @NotBlank @Size(max = 50) String firstName,
        @NotBlank @Size(max = 50) String lastName,
        String phone
) {}

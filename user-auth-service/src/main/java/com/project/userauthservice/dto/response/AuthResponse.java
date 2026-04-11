package com.project.userauthservice.dto.response;


public record AuthResponse(
        String accessToken,
        String refreshToken,
        String userId,
        String email,
        String firstName,
        String lastName
) {}
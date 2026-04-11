package com.project.userauthservice.service;



import com.project.userauthservice.domain.entity.User;
import com.project.userauthservice.domain.repository.UserRepository;
import com.project.userauthservice.exception.*;
import com.project.userauthservice.kafka.UserEventPublisher;
import com.project.userauthservice.security.JwtService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder  passwordEncoder;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new DuplicateEmailException(req.email());
        }

        User user = User.builder()
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .firstName(req.firstName())
                .lastName(req.lastName())
                .phone(req.phone())
                .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getEmail());
        eventPublisher.publishUserRegistered(user);

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BadCredentialsException("Account is not active");
        }

        log.info("User logged in: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(RefreshTokenRequest req) {
        Claims claims;
        try {
            claims = jwtService.validateToken(req.refreshToken());
        } catch (Exception ex) {
            throw new InvalidTokenException("Refresh token is invalid or expired");
        }

        String userId = claims.getSubject();

        if (!jwtService.isRefreshTokenValid(userId, req.refreshToken())) {
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        // Rotate: revoke old, issue new pair
        jwtService.revokeRefreshToken(userId);
        log.info("Token refreshed for user: {}", user.getEmail());
        return buildAuthResponse(user);
    }

    public void logout(String accessToken) {
        String userId = jwtService.extractUserId(accessToken);
        jwtService.blacklistToken(accessToken);
        jwtService.revokeRefreshToken(userId);
        log.info("User {} logged out", userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                jwtService.generateRefreshToken(user),
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );
    }


}

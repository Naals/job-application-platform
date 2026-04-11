package com.project.userauthservice.security;


import com.project.userauthservice.domain.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtService {

    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.access-token-expiry-ms:900000}")
    private long accessTokenExpiryMs;

    @Value("${jwt.refresh-token-expiry-ms:604800000}")
    private long refreshTokenExpiryMs;

    private static final String REFRESH_PREFIX   = "refresh:token:";
    private static final String BLACKLIST_PREFIX  = "blacklist:token:";



    public String generateAccessToken(User user) {
        String roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email",     user.getEmail())
                .claim("roles",     roles)
                .claim("firstName", user.getFirstName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                .signWith(signingKey())
                .compact();
    }

    public String generateRefreshToken(User user) {
        String token = Jwts.builder()
                .subject(user.getId().toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
                .signWith(signingKey())
                .compact();

        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + user.getId(),
                token,
                Duration.ofMillis(refreshTokenExpiryMs)
        );
        return token;
    }



    public Claims validateToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUserId(String token) {
        return validateToken(token).getSubject();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
}

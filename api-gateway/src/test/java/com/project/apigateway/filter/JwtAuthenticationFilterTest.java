package com.project.apigateway.filter;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtAuthenticationFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @MockBean
    private ReactiveValueOperations<String, String> valueOps;

    private static final String SECRET =
            "my-super-secret-key-must-be-at-least-32-chars";

    @BeforeEach
    void setup() {
        when(redisTemplate.hasKey(anyString())).thenReturn(Mono.just(false));
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @Test
    void validToken_shouldPassThrough() {
        String token = buildToken(30_000);
        webTestClient.get().uri("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isNotFound(); // 404, not 401 — filter passed
    }

    @Test
    void expiredToken_shouldReturn401() {
        String token = buildToken(-1_000); // already expired
        webTestClient.get().uri("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void blacklistedToken_shouldReturn401() {
        String token = buildToken(30_000);
        when(redisTemplate.hasKey("blacklist:token:" + token))
                .thenReturn(Mono.just(true));

        webTestClient.get().uri("/api/v1/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void missingAuthHeader_shouldReturn401() {
        webTestClient.get().uri("/api/v1/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    private String buildToken(long expiryOffsetMs) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("user-uuid-123")
                .claim("roles", "ROLE_CANDIDATE")
                .claim("email", "test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryOffsetMs))
                .signWith(key)
                .compact();
    }
}

package com.project.apigateway.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component("redisCustomHealth")
@RequiredArgsConstructor
public class RedisHealthIndicator implements ReactiveHealthIndicator {

    private final ReactiveRedisTemplate<String, String> redisTemplate;

    @Override
    public Mono<Health> health() {
        return redisTemplate.getConnectionFactory()
                .getReactiveConnection()
                .ping()
                .map(pong -> Health.up().withDetail("ping", pong).build())
                .onErrorResume(ex -> {
                    log.warn("Redis health check failed: {}", ex.getMessage());
                    return Mono.just(Health.down().withDetail("error", ex.getMessage()).build());
                });
    }
}
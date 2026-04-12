package com.project.jobpostingservice.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String JOBS_CACHE     = "jobs";
    public static final String JOB_LIST_CACHE = "job-list";

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(JOBS_CACHE,
                        defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withCacheConfiguration(JOB_LIST_CACHE,
                        defaultConfig.entryTtl(Duration.ofMinutes(5)))
                .build();
    }
}

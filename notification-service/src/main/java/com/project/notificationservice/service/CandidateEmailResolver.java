package com.project.notificationservice.service;

import com.project.notificationservice.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateEmailResolver {

    private final UserClient userClient;

    @Cacheable("candidate-emails")
    public String resolve(String candidateId) {
        try {
            UserClient.UserDto user = userClient.getUserById(candidateId);
            return user != null ? user.email() : null;
        } catch (Exception ex) {
            log.warn("Failed to resolve email for candidateId {}: {}", candidateId, ex.getMessage());
            return null;
        }
    }
}

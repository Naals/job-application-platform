package com.project.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Resolves a candidate UUID → email address.
 */
@Slf4j
@Service
public class CandidateEmailResolver {

    /**
     * Returns the email for the given candidateId.
     * Replace this stub with a real Feign/HTTP call to user-auth-service.
     */
    @Cacheable("candidate-emails")
    public String resolve(String candidateId) {

        log.debug("Resolving email for candidateId: {}", candidateId);
        return null; // stub — returns null until Feign is wired
    }
}

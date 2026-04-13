package com.project.applicationservice.kafka;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationSubmittedEvent(
        UUID          applicationId,
        UUID          candidateId,
        UUID          jobId,
        String        jobTitle,
        String        companyName,
        LocalDateTime submittedAt
) {}

package com.project.notificationservice.event;

import java.time.LocalDateTime;

public record ApplicationSubmittedEvent(
        String        applicationId,
        String        candidateId,
        String        jobId,
        String        jobTitle,
        String        companyName,
        LocalDateTime submittedAt
) {}
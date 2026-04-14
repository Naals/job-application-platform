package com.project.notificationservice.event;

import java.time.LocalDateTime;

public record ApplicationStatusChangedEvent(
        String        applicationId,
        String        candidateId,
        String        jobId,
        String        jobTitle,
        String        companyName,
        String        previousStatus,
        String        newStatus,
        String        reason,
        LocalDateTime changedAt
) {}

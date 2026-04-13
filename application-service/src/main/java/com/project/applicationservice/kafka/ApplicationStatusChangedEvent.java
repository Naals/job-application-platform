package com.project.applicationservice.kafka;


import com.project.applicationservice.domain.entity.ApplicationStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationStatusChangedEvent(
        UUID              applicationId,
        UUID              candidateId,
        UUID              jobId,
        String            jobTitle,
        String            companyName,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        String            reason,
        LocalDateTime     changedAt
) {}

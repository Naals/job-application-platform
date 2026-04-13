package com.project.applicationservice.dto.response;


import com.project.applicationservice.domain.entity.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ApplicationResponse(
        UUID              id,
        UUID              candidateId,
        UUID              jobId,
        String            jobTitle,
        String            companyName,
        ApplicationStatus status,
        String            coverLetter,
        UUID              resumeId,
        String            notes,
        LocalDateTime     appliedAt,
        LocalDateTime     updatedAt
) {}

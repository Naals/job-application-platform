package com.project.applicationservice.dto.response;


import com.project.applicationservice.domain.entity.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record StatusHistoryResponse(
        ApplicationStatus fromStatus,
        ApplicationStatus toStatus,
        String            reason,
        UUID              changedBy,
        LocalDateTime     changedAt
) {}

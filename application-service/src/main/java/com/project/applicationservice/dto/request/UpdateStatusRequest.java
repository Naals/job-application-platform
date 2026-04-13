package com.project.applicationservice.dto.request;

import com.project.applicationservice.domain.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull ApplicationStatus status,
        String reason
) {}

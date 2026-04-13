package com.project.applicationservice.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ApplyRequest(
        @NotNull UUID   jobId,
        String          coverLetter,
        UUID            resumeId
) {}

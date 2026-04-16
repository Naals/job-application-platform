package com.project.resumeservice.dto.response;

import com.project.resumeservice.domain.entity.Resume;
import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeResponse(
        UUID                 id,
        UUID                 candidateId,
        String               originalFileName,
        String               contentType,
        Long                 fileSizeBytes,
        Resume.ParseStatus   parseStatus,
        Boolean              isActive,
        LocalDateTime        uploadedAt
) {}

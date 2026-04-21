package com.project.notificationservice.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record ResumeUploadedEvent(
        UUID          resumeId,
        UUID          candidateId,
        String        originalFileName,
        String        contentType,
        Long          fileSizeBytes,
        String        objectKey,
        LocalDateTime uploadedAt
) {}

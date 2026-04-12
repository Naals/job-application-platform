package com.project.jobpostingservice.kafka;


import java.time.LocalDateTime;
import java.util.UUID;

public record JobClosedEvent(
        UUID          jobId,
        UUID          employerId,
        LocalDateTime closedAt
) {}

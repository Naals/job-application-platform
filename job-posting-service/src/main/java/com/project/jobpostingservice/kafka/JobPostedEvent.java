package com.project.jobpostingservice.kafka;


import java.time.LocalDateTime;
import java.util.UUID;

public record JobPostedEvent(
        UUID          jobId,
        String        title,
        String        company,
        String        location,
        String        jobType,
        String        experienceLevel,
        Boolean       remote,
        UUID          employerId,
        LocalDateTime postedAt
) {}

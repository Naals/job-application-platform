package com.project.jobpostingservice.dto.response;


import com.project.jobpostingservice.domain.entity.Job;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record JobResponse(
        UUID              id,
        String            title,
        String            description,
        String            company,
        UUID              employerId,
        String            location,
        Job.JobType       jobType,
        Job.JobStatus     status,
        Job.ExperienceLevel experienceLevel,
        BigDecimal        salaryMin,
        BigDecimal        salaryMax,
        String            currency,
        String            requirements,
        String            benefits,
        Boolean           remote,
        LocalDateTime     expiresAt,
        LocalDateTime     createdAt,
        LocalDateTime     updatedAt
) {}

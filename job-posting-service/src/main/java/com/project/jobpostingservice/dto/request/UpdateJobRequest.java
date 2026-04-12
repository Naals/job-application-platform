package com.project.jobpostingservice.dto.request;

import com.project.jobpostingservice.domain.entity.Job;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UpdateJobRequest(
        @Size(max = 150) String title,
        String           description,
        @Size(max = 100) String location,
        Job.JobType      jobType,
        Job.ExperienceLevel experienceLevel,
        BigDecimal       salaryMin,
        BigDecimal       salaryMax,
        String           requirements,
        String           benefits,
        Boolean          remote,
        LocalDateTime    expiresAt
) {}

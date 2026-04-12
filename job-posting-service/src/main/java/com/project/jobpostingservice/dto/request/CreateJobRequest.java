package com.project.jobpostingservice.dto.request;


import com.project.jobpostingservice.domain.entity.Job;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateJobRequest(
        @NotBlank @Size(max = 150) String title,
        @NotBlank                  String description,
        @NotBlank @Size(max = 100) String company,
        @Size(max = 100)           String location,
        Job.JobType                jobType,
        Job.ExperienceLevel        experienceLevel,
        @DecimalMin("0") BigDecimal salaryMin,
        @DecimalMin("0") BigDecimal salaryMax,
        String                     currency,
        String                     requirements,
        String                     benefits,
        Boolean                    remote,
        LocalDateTime              expiresAt
) {
}

package com.project.jobpostingservice.search;

import java.math.BigDecimal;

public record JobSearchRequest(
        String keyword,
        String location,
        String jobType,
        String experienceLevel,
        Boolean remote,
        BigDecimal salaryMin,
        BigDecimal salaryMax
) {}
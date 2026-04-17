package com.project.applicationservice.client;


import com.project.applicationservice.client.dto.JobDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name     = "job-posting-service",
        url      = "${job-posting-service.url}",
        fallback = JobClientFallback.class
)
public interface JobClient {

    @GetMapping("/api/v1/jobs/{id}")
    @CircuitBreaker(name = "job-posting-service", fallbackMethod = "getJobFallback")
    @Retry(name = "job-posting-service")
    JobDto getJobById(@PathVariable UUID id);
}

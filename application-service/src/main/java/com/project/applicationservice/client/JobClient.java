package com.project.applicationservice.client;


import com.project.applicationservice.client.dto.JobDto;
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
    JobDto getJobById(@PathVariable UUID id);
}

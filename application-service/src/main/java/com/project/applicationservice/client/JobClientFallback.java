package com.project.applicationservice.client;

import com.project.applicationservice.client.dto.JobDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class JobClientFallback implements JobClient {

    @Override
    public JobDto getJobById(UUID id) {
        log.warn("JobClient fallback triggered for jobId: {}", id);
        return null;  // caller checks for null and throws JobServiceUnavailableException
    }
}

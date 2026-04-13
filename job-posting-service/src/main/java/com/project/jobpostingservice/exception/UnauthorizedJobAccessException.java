package com.project.jobpostingservice.exception;

import java.util.UUID;

public class UnauthorizedJobAccessException extends RuntimeException {
    public UnauthorizedJobAccessException(UUID jobId) {
        super("You do not have permission to modify job: " + jobId);
    }
}

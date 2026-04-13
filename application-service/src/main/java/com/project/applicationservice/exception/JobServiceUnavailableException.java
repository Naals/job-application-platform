package com.project.applicationservice.exception;

public class JobServiceUnavailableException extends RuntimeException {
    public JobServiceUnavailableException(String jobId) {
        super("Job service unavailable or job not found: " + jobId);
    }
}

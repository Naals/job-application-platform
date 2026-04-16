package com.project.resumeservice.exception;

import java.util.UUID;

public class ResumeNotFoundException extends RuntimeException {
    public ResumeNotFoundException(UUID id) {
        super("Resume not found: " + id);
    }
}

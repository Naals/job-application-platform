package com.project.applicationservice.exception;

import java.util.UUID;

public class UnauthorizedApplicationAccessException extends RuntimeException {
    public UnauthorizedApplicationAccessException(UUID applicationId) {
        super("You do not have permission to modify application: " + applicationId);
    }
}

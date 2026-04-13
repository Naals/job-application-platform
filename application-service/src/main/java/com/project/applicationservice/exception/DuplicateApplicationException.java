package com.project.applicationservice.exception;


import java.util.UUID;

public class DuplicateApplicationException extends RuntimeException {
    public DuplicateApplicationException(UUID candidateId, UUID jobId) {
        super("Candidate " + candidateId + " has already applied to job " + jobId);
    }
}

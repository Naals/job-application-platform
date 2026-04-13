package com.project.applicationservice.exception;


import com.project.applicationservice.domain.entity.ApplicationStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}

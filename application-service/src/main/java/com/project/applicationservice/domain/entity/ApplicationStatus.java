package com.project.applicationservice.domain.entity;

import java.util.Set;

public enum ApplicationStatus {

    SUBMITTED,
    UNDER_REVIEW,
    INTERVIEW_SCHEDULED,
    OFFER_EXTENDED,
    ACCEPTED,
    REJECTED,
    WITHDRAWN;

    // FSM: defines which transitions are legal from each state
    public Set<ApplicationStatus> allowedTransitions() {
        return switch (this) {
            case SUBMITTED          -> Set.of(UNDER_REVIEW, REJECTED, WITHDRAWN);
            case UNDER_REVIEW       -> Set.of(INTERVIEW_SCHEDULED, REJECTED, WITHDRAWN);
            case INTERVIEW_SCHEDULED -> Set.of(OFFER_EXTENDED, REJECTED, WITHDRAWN);
            case OFFER_EXTENDED     -> Set.of(ACCEPTED, REJECTED, WITHDRAWN);
            case ACCEPTED, REJECTED, WITHDRAWN -> Set.of(); // terminal states
        };
    }

    public boolean canTransitionTo(ApplicationStatus next) {
        return allowedTransitions().contains(next);
    }
}

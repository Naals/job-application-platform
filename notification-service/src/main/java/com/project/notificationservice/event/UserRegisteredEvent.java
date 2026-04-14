package com.project.notificationservice.event;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        String        userId,
        String        email,
        String        firstName,
        String        lastName,
        LocalDateTime registeredAt
) {}

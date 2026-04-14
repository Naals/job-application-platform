package com.project.notificationservice.consumer;

import com.project.notificationservice.event.UserRegisteredEvent;
import com.project.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final EmailService emailService;

    @KafkaListener(
            topics         = "user.registered",
            groupId        = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onUserRegistered(UserRegisteredEvent event) {
        log.info("Received UserRegisteredEvent for: {}", event.email());

        emailService.sendHtmlEmail(
                event.email(),
                "Welcome to Job Platform!",
                "welcome",
                Map.of(
                        "firstName", event.firstName(),
                        "lastName",  event.lastName(),
                        "userId",    event.userId()
                )
        );
    }
}

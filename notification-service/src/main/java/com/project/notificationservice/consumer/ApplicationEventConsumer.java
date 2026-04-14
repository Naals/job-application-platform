package com.project.notificationservice.consumer;

import com.project.notificationservice.event.ApplicationStatusChangedEvent;
import com.project.notificationservice.event.ApplicationSubmittedEvent;
import com.project.notificationservice.service.CandidateEmailResolver;
import com.project.notificationservice.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventConsumer {

    private final EmailService           emailService;
    private final CandidateEmailResolver emailResolver;

    @KafkaListener(
            topics           = "application.submitted",
            groupId          = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onApplicationSubmitted(ApplicationSubmittedEvent event) {
        log.info("Received ApplicationSubmittedEvent: {}", event.applicationId());

        String candidateEmail = emailResolver.resolve(event.candidateId());
        if (candidateEmail == null) {
            log.warn("Could not resolve email for candidateId: {}", event.candidateId());
            return;
        }

        emailService.sendHtmlEmail(
                candidateEmail,
                "Your application to " + event.companyName() + " has been received",
                "application-submitted",
                Map.of(
                        "jobTitle",    event.jobTitle(),
                        "companyName", event.companyName(),
                        "submittedAt", event.submittedAt().toString(),
                        "applicationId", event.applicationId()
                )
        );
    }

    @KafkaListener(
            topics           = "application.status.changed",
            groupId          = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onStatusChanged(ApplicationStatusChangedEvent event) {
        log.info("Received ApplicationStatusChangedEvent: {} → {}",
                event.previousStatus(), event.newStatus());

        String candidateEmail = emailResolver.resolve(event.candidateId());
        if (candidateEmail == null) {
            log.warn("Could not resolve email for candidateId: {}", event.candidateId());
            return;
        }

        emailService.sendHtmlEmail(
                candidateEmail,
                "Update on your application to " + event.companyName(),
                "status-changed",
                Map.of(
                        "jobTitle",        event.jobTitle(),
                        "companyName",     event.companyName(),
                        "previousStatus",  event.previousStatus(),
                        "newStatus",       event.newStatus(),
                        "reason",          event.reason() != null ? event.reason() : "",
                        "applicationId",   event.applicationId()
                )
        );
    }
}

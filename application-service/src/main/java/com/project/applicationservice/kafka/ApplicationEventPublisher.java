package com.project.applicationservice.kafka;


import com.project.applicationservice.domain.entity.Application;
import com.project.applicationservice.domain.entity.ApplicationStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String SUBMITTED_TOPIC     = "application.submitted";
    private static final String STATUS_CHANGED_TOPIC = "application.status.changed";

    public void publishApplicationSubmitted(Application app) {
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                app.getId(), app.getCandidateId(), app.getJobId(),
                app.getJobTitle(), app.getCompanyName(), LocalDateTime.now());

        kafkaTemplate.send(SUBMITTED_TOPIC, app.getId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("Failed to publish ApplicationSubmittedEvent: {}", ex.getMessage());
                    else
                        log.debug("Published ApplicationSubmittedEvent: {}", app.getId());
                });
    }

    public void publishStatusChanged(Application app,
                                     ApplicationStatus from,
                                     ApplicationStatus to,
                                     String reason) {
        ApplicationStatusChangedEvent event = new ApplicationStatusChangedEvent(
                app.getId(), app.getCandidateId(), app.getJobId(),
                app.getJobTitle(), app.getCompanyName(),
                from, to, reason, LocalDateTime.now());

        kafkaTemplate.send(STATUS_CHANGED_TOPIC, app.getId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("Failed to publish StatusChangedEvent: {}", ex.getMessage());
                    else
                        log.debug("Published StatusChangedEvent {} → {}: {}", from, to, app.getId());
                });
    }
}

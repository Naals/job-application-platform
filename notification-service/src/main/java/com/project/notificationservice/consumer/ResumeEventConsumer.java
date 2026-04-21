package com.project.notificationservice.consumer;

import com.project.notificationservice.event.ResumeUploadedEvent;
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
public class ResumeEventConsumer {

    private final EmailService           emailService;
    private final CandidateEmailResolver emailResolver;

    @KafkaListener(
            topics           = "resume.uploaded",
            groupId          = "notification-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onResumeUploaded(ResumeUploadedEvent event) {
        log.info("Received ResumeUploadedEvent: resumeId={}", event.resumeId());

        String email = emailResolver.resolve(event.candidateId().toString());
        if (email == null) {
            log.warn("Could not resolve email for candidateId: {}", event.candidateId());
            return;
        }

        emailService.sendHtmlEmail(
                email,
                "Your resume has been uploaded successfully",
                "resume-uploaded",
                Map.of(
                        "fileName",   event.originalFileName(),
                        "uploadedAt", event.uploadedAt().toString()
                )
        );
    }
}

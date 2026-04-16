package com.project.resumeservice.kafka;

import com.project.resumeservice.domain.entity.Resume;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String RESUME_UPLOADED_TOPIC = "resume.uploaded";

    public void publishResumeUploaded(Resume resume) {
        ResumeUploadedEvent event = new ResumeUploadedEvent(
                resume.getId(),
                resume.getCandidateId(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getFileSizeBytes(),
                resume.getObjectKey(),
                LocalDateTime.now()
        );

        kafkaTemplate.send(RESUME_UPLOADED_TOPIC,
                        resume.getCandidateId().toString(),
                        event)
                .whenComplete((r, ex) -> {
                    if (ex != null)
                        log.error("Failed to publish ResumeUploadedEvent: {}", ex.getMessage());
                    else
                        log.debug("Published ResumeUploadedEvent: {}", resume.getId());
                });
    }
}
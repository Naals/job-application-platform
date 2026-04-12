package com.project.jobpostingservice.kafka;


import com.project.jobpostingservice.domain.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String JOB_POSTED_TOPIC = "job.posted";
    private static final String JOB_CLOSED_TOPIC = "job.closed";

    public void publishJobPosted(Job job) {
        JobPostedEvent event = new JobPostedEvent(
                job.getId(),
                job.getTitle(),
                job.getCompany(),
                job.getLocation(),
                job.getJobType().name(),
                job.getExperienceLevel().name(),
                job.getRemote(),
                job.getEmployerId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(JOB_POSTED_TOPIC, job.getId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish JobPostedEvent: {}", ex.getMessage());
                    else log.debug("Published JobPostedEvent: {}", job.getId());
                });
    }

    public void publishJobClosed(Job job) {
        JobClosedEvent event = new JobClosedEvent(
                job.getId(),
                job.getEmployerId(),
                LocalDateTime.now()
        );
        kafkaTemplate.send(JOB_CLOSED_TOPIC, job.getId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("Failed to publish JobClosedEvent: {}", ex.getMessage());
                    else log.debug("Published JobClosedEvent: {}", job.getId());
                });
    }
}

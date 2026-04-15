package com.project.notificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes messages that exhausted all retries.
 * Spring Kafka publishes these to a topic named <original-topic>.DLT

 * Covered topics:
 *   user.registered.DLT
 *   application.submitted.DLT
 *   application.status.changed.DLT
 */
@Slf4j
@Component
public class DeadLetterConsumer {

    @KafkaListener(
            topicPattern     = ".*\\.DLT",
            groupId          = "notification-dlt-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDeadLetter(ConsumerRecord<String, Object> record) {
        log.error(
                "DEAD LETTER — topic={} partition={} offset={} key={} payload={}",
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                record.value()
        );
    }
}
package com.project.userauthservice.kafka;

import com.project.userauthservice.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String USER_REGISTERED_TOPIC = "user.registered";

    public void publishUserRegistered(User user) {
        UserRegisteredEvent event = new UserRegisteredEvent(
                user.getId().toString(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                LocalDateTime.now()
        );

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(USER_REGISTERED_TOPIC, user.getId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish UserRegisteredEvent for {}: {}",
                        user.getEmail(), ex.getMessage());
            } else {
                log.debug("Published UserRegisteredEvent for {} to partition {}",
                        user.getEmail(),
                        result.getRecordMetadata().partition());
            }
        });
    }
}

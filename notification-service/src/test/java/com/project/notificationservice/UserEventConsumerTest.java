package com.project.notificationservice;

import com.project.notificationservice.consumer.UserEventConsumer;
import com.project.notificationservice.event.UserRegisteredEvent;
import com.project.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock EmailService emailService;
    @InjectMocks UserEventConsumer consumer;

    @Test
    void onUserRegistered_sendsWelcomeEmail() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                "user-uuid-1",
                "john@example.com",
                "John",
                "Doe",
                LocalDateTime.now()
        );

        consumer.onUserRegistered(event);

        verify(emailService).sendHtmlEmail(
                eq("john@example.com"),
                contains("Welcome"),
                eq("welcome"),
                argThat(vars ->
                        vars.get("firstName").equals("John") &&
                                vars.get("lastName").equals("Doe")
                )
        );
    }
}

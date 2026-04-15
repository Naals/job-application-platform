package com.project.notificationservice;

import com.project.notificationservice.consumer.ApplicationEventConsumer;
import com.project.notificationservice.event.ApplicationStatusChangedEvent;
import com.project.notificationservice.event.ApplicationSubmittedEvent;
import com.project.notificationservice.service.CandidateEmailResolver;
import com.project.notificationservice.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationEventConsumerTest {

    @Mock EmailService           emailService;
    @Mock CandidateEmailResolver emailResolver;
    @InjectMocks ApplicationEventConsumer consumer;

    @Test
    void onApplicationSubmitted_sendsEmail() {
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                "app-id-1", "candidate-id-1", "job-id-1",
                "Java Developer", "Acme Corp", LocalDateTime.now()
        );

        when(emailResolver.resolve("candidate-id-1"))
                .thenReturn("john@example.com");

        consumer.onApplicationSubmitted(event);

        verify(emailService).sendHtmlEmail(
                eq("john@example.com"),
                contains("Acme Corp"),
                eq("application-submitted"),
                argThat(vars ->
                        vars.get("jobTitle").equals("Java Developer") &&
                                vars.get("companyName").equals("Acme Corp")
                )
        );
    }

    @Test
    void onApplicationSubmitted_unresolvedEmail_skipsEmail() {
        ApplicationSubmittedEvent event = new ApplicationSubmittedEvent(
                "app-id-2", "unknown-candidate", "job-id-2",
                "PM", "Corp", LocalDateTime.now()
        );

        when(emailResolver.resolve("unknown-candidate")).thenReturn(null);

        consumer.onApplicationSubmitted(event);

        verifyNoInteractions(emailService);
    }

    @Test
    void onStatusChanged_accepted_sendsEmail() {
        ApplicationStatusChangedEvent event = new ApplicationStatusChangedEvent(
                "app-id-3", "candidate-id-1", "job-id-1",
                "Java Developer", "Acme Corp",
                "OFFER_EXTENDED", "ACCEPTED",
                null, LocalDateTime.now()
        );

        when(emailResolver.resolve("candidate-id-1"))
                .thenReturn("john@example.com");

        consumer.onStatusChanged(event);

        verify(emailService).sendHtmlEmail(
                eq("john@example.com"),
                contains("Acme Corp"),
                eq("status-changed"),
                argThat(vars ->
                        vars.get("newStatus").equals("ACCEPTED") &&
                                vars.get("previousStatus").equals("OFFER_EXTENDED")
                )
        );
    }
}

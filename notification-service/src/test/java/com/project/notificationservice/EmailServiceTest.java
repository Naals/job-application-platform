package com.project.notificationservice;

import com.project.notificationservice.config.MailConfig;
import com.project.notificationservice.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender  mailSender;
    @Mock TemplateEngine  templateEngine;
    @Mock MimeMessage     mimeMessage;

    @InjectMocks EmailService emailService;

    @BeforeEach
    void setup() {
        MailConfig config   = new MailConfig();
        config.fromEmail    = "noreply@jobplatform.com";
        config.fromName     = "Job Platform";
        config.platformUrl  = "https://jobplatform.com";

        emailService = new EmailService(mailSender, templateEngine, config);
    }

    @Test
    void sendHtmlEmail_callsMailSender() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("welcome"), any(Context.class)))
                .thenReturn("<html><body>Welcome!</body></html>");

        emailService.sendHtmlEmail(
                "user@example.com",
                "Welcome!",
                "welcome",
                Map.of("firstName", "John", "lastName", "Doe", "userId", "uuid-1")
        );

        verify(mailSender).send(mimeMessage);
        verify(templateEngine).process(eq("welcome"), any(Context.class));
    }

    @Test
    void sendHtmlEmail_templateEngineFailure_throwsRuntimeException() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process((String) any(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        org.assertj.core.api.Assertions
                .assertThatThrownBy(() -> emailService.sendHtmlEmail(
                        "user@example.com", "Test", "missing-template", Map.of()))
                .isInstanceOf(RuntimeException.class);
    }
}

package com.project.notificationservice.service;

import com.project.notificationservice.config.MailConfig;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailConfig     mailConfig;

    /**
     * Renders a Thymeleaf template and sends it as an async HTML email.
     *
     * @param to           recipient address
     * @param subject      email subject line
     * @param templateName template filename (without .html suffix)
     * @param variables    variables injected into the Thymeleaf context
     */
    @Async
    public void sendHtmlEmail(String to,
                              String subject,
                              String templateName,
                              Map<String, Object> variables) {
        try {
            Context ctx = new Context();
            ctx.setVariable("platformUrl", mailConfig.platformUrl);
            ctx.setVariable("fromName",    mailConfig.fromName);
            variables.forEach(ctx::setVariable);

            String htmlBody = templateEngine.process(templateName, ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailConfig.fromEmail, mailConfig.fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("Email sent to {} — subject: {}", to, subject);

        } catch (MessagingException | java.io.UnsupportedEncodingException ex) {
            log.error("Failed to send email to {}: {}", to, ex.getMessage());
            throw new RuntimeException("Email send failure", ex);
        }
    }
}
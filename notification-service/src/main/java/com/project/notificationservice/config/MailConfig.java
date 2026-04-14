package com.project.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MailConfig {

    @Value("${notification.from-email}")
    public String fromEmail;

    @Value("${notification.from-name}")
    public String fromName;

    @Value("${notification.platform-url}")
    public String platformUrl;
}

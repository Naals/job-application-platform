package com.project.applicationservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic applicationSubmittedTopic() {
        return TopicBuilder.name("application.submitted").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic applicationStatusChangedTopic() {
        return TopicBuilder.name("application.status.changed").partitions(3).replicas(1).build();
    }
}

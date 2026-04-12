package com.project.jobpostingservice.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic jobPostedTopic() {
        return TopicBuilder.name("job.posted").partitions(3).replicas(1).build();
    }

    @Bean
    public NewTopic jobClosedTopic() {
        return TopicBuilder.name("job.closed").partitions(3).replicas(1).build();
    }
}
package com.project.jobpostingservice.health;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.*;
import org.springframework.stereotype.Component;

@Slf4j
@Component("elasticsearchCustomHealth")
@RequiredArgsConstructor
public class ElasticsearchHealthIndicator implements HealthIndicator {

    private final ElasticsearchClient esClient;

    @Override
    public Health health() {
        try {
            var health = esClient.cluster().health();
            return Health.up()
                    .withDetail("status",        health.status().jsonValue())
                    .withDetail("numberOfNodes", health.numberOfNodes())
                    .build();
        } catch (Exception ex) {
            log.warn("Elasticsearch health check failed: {}", ex.getMessage());
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .build();
        }
    }
}

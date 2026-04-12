package com.project.jobpostingservice.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface JobSearchRepository
        extends ElasticsearchRepository<JobDocument, String> {
}

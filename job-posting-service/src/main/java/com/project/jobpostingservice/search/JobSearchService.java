package com.project.jobpostingservice.search;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final ElasticsearchClient esClient;
    private final JobSearchRepository searchRepository;

    public Page<JobDocument> search(JobSearchRequest req, Pageable pageable) {
        List<Query> filters = new ArrayList<>();

        // Only published jobs are visible in public search
        filters.add(Query.of(q -> q
                .term(t -> t.field("status").value("PUBLISHED"))));

        if (hasText(req.location())) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("location").value(req.location()))));
        }
        if (hasText(req.jobType())) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("jobType").value(req.jobType()))));
        }
        if (hasText(req.experienceLevel())) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("experienceLevel").value(req.experienceLevel()))));
        }
        if (Boolean.TRUE.equals(req.remote())) {
            filters.add(Query.of(q -> q
                    .term(t -> t.field("remote").value(true))));
        }
        if (req.salaryMin() != null) {
            filters.add(Query.of(q -> q.range(r -> r
                    .field("salaryMax")
                    .gte(JsonData.of(req.salaryMin()))
            )));
        }
        if (req.salaryMax() != null) {
            filters.add(Query.of(q -> q.range(r -> r
                    .field("salaryMin")
                    .lte(JsonData.of(req.salaryMax()))
            )));
        }

        // Full-text query: keyword hits title (3x boost), description (2x), requirements
        Query textQuery = hasText(req.keyword())
                ? Query.of(q -> q.multiMatch(m -> m
                                                  .query(req.keyword())
                                                  .fields("title^3", "description^2", "requirements")
                                                  .type(TextQueryType.BestFields)
                                                  .fuzziness("AUTO")))
                : Query.of(q -> q.matchAll(m -> m));

        Query finalQuery = Query.of(q -> q.bool(b -> b
                .must(textQuery)
                .filter(filters)));

        try {
            SearchResponse<JobDocument> response = esClient.search(s -> s
                            .index("jobs")
                            .query(finalQuery)
                            .from((int) pageable.getOffset())
                            .size(pageable.getPageSize()),
                    JobDocument.class);

            List<JobDocument> hits = response.hits().hits().stream()
                    .map(Hit::source)
                    .toList();

            long total = response.hits().total() != null
                    ? response.hits().total().value() : 0L;

            return new PageImpl<>(hits, pageable, total);

        } catch (IOException ex) {
            log.error("Elasticsearch search failed", ex);
            throw new RuntimeException("Search temporarily unavailable", ex);
        }
    }

    public void indexJob(JobDocument doc) {
        searchRepository.save(doc);
        log.debug("Indexed job: {}", doc.getId());
    }

    public void removeFromIndex(String jobId) {
        searchRepository.deleteById(jobId);
        log.debug("Removed job from index: {}", jobId);
    }

    private boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
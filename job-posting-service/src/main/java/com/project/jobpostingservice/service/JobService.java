package com.project.jobpostingservice.service;


import com.project.jobpostingservice.config.CacheConfig;
import com.project.jobpostingservice.domain.entity.Job;
import com.project.jobpostingservice.domain.repository.JobRepository;
import com.project.jobpostingservice.dto.request.CreateJobRequest;
import com.project.jobpostingservice.dto.request.UpdateJobRequest;
import com.project.jobpostingservice.dto.response.JobResponse;
import com.project.jobpostingservice.exception.JobNotFoundException;
import com.project.jobpostingservice.exception.UnauthorizedJobAccessException;
import com.project.jobpostingservice.kafka.JobEventPublisher;
import com.project.jobpostingservice.mapper.JobMapper;
import com.project.jobpostingservice.search.JobSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository    jobRepository;
    private final JobMapper        jobMapper;
    private final JobSearchService searchService;
    private final JobEventPublisher eventPublisher;

    @Transactional
    public JobResponse create(CreateJobRequest req, UUID employerId) {
        Job job = jobMapper.toEntity(req);
        job.setEmployerId(employerId);
        job = jobRepository.save(job);
        log.info("Job created: {} by employer: {}", job.getId(), employerId);
        return jobMapper.toResponse(job);
    }

    @Cacheable(value = CacheConfig.JOBS_CACHE, key = "#id")
    public JobResponse findById(UUID id) {
        return jobRepository.findById(id)
                .map(jobMapper::toResponse)
                .orElseThrow(() -> new JobNotFoundException(id));
    }

    public Page<JobResponse> findByEmployer(UUID employerId, Pageable pageable) {
        return jobRepository.findByEmployerId(employerId, pageable)
                .map(jobMapper::toResponse);
    }

    @Transactional
    @CachePut(value = CacheConfig.JOBS_CACHE, key = "#id")
    public JobResponse update(UUID id, UpdateJobRequest req, UUID employerId) {
        Job job = getJobOwnedBy(id, employerId);
        jobMapper.updateEntity(req, job);
        job = jobRepository.save(job);
        log.info("Job updated: {}", id);
        return jobMapper.toResponse(job);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.JOBS_CACHE, key = "#id")
    public JobResponse publish(UUID id, UUID employerId) {
        Job job = getJobOwnedBy(id, employerId);

        if (job.getStatus() != Job.JobStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT jobs can be published");
        }

        job.setStatus(Job.JobStatus.PUBLISHED);
        job = jobRepository.save(job);

        // Index in Elasticsearch
        searchService.indexJob(jobMapper.toDocument(job));

        // Notify via Kafka
        eventPublisher.publishJobPosted(job);

        log.info("Job published: {}", id);
        return jobMapper.toResponse(job);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.JOBS_CACHE, key = "#id")
    public JobResponse close(UUID id, UUID employerId) {
        Job job = getJobOwnedBy(id, employerId);

        if (job.getStatus() != Job.JobStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED jobs can be closed");
        }

        job.setStatus(Job.JobStatus.CLOSED);
        job = jobRepository.save(job);

        // Remove from search index
        searchService.removeFromIndex(id.toString());

        // Notify via Kafka
        eventPublisher.publishJobClosed(job);

        log.info("Job closed: {}", id);
        return jobMapper.toResponse(job);
    }

    @Transactional
    @CacheEvict(value = CacheConfig.JOBS_CACHE, key = "#id")
    public void delete(UUID id, UUID employerId) {
        Job job = getJobOwnedBy(id, employerId);
        jobRepository.delete(job);
        searchService.removeFromIndex(id.toString());
        log.info("Job deleted: {}", id);
    }

    // ── Helpers ──────────────────────────────────────────

    private Job getJobOwnedBy(UUID jobId, UUID employerId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException(jobId));
        if (!job.getEmployerId().equals(employerId)) {
            throw new UnauthorizedJobAccessException(jobId);
        }
        return job;
    }
}

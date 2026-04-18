package com.project.applicationservice.service;

import com.project.applicationservice.client.JobClient;
import com.project.applicationservice.client.dto.JobDto;
import com.project.applicationservice.config.CacheConfig;
import com.project.applicationservice.domain.entity.*;
import com.project.applicationservice.domain.repository.*;
import com.project.applicationservice.dto.request.ApplyRequest;
import com.project.applicationservice.dto.request.UpdateStatusRequest;
import com.project.applicationservice.dto.response.ApplicationResponse;
import com.project.applicationservice.exception.*;
import com.project.applicationservice.kafka.ApplicationEventPublisher;
import com.project.applicationservice.mapper.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository              applicationRepository;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationMapper                  mapper;
    private final JobClient                          jobClient;
    private final ApplicationEventPublisher          eventPublisher;

    // ── apply

    @Transactional
    @CacheEvict(value = CacheConfig.CANDIDATE_APPS_CACHE, key = "#candidateId")
    public ApplicationResponse apply(ApplyRequest req, UUID candidateId) {

        if (applicationRepository.existsByCandidateIdAndJobId(candidateId, req.jobId())) {
            throw new DuplicateApplicationException(candidateId, req.jobId());
        }

        JobDto job = jobClient.getJobById(req.jobId());
        if (job == null) {
            throw new JobServiceUnavailableException(req.jobId().toString());
        }
        if (!"PUBLISHED".equals(job.status())) {
            throw new IllegalStateException("Cannot apply to a job that is not PUBLISHED");
        }

        Application application = Application.builder()
                .candidateId(candidateId)
                .jobId(req.jobId())
                .jobTitle(job.title())
                .companyName(job.company())
                .coverLetter(req.coverLetter())
                .resumeId(req.resumeId())
                .build();

        application = applicationRepository.save(application);

        // FIX 4 — no null fromStatus; skip history for initial submission
        // The appliedAt timestamp on the entity already records when it was submitted.
        // History is only for transitions AFTER submission.

        eventPublisher.publishApplicationSubmitted(application);
        log.info("Application submitted: {} by candidate: {}", application.getId(), candidateId);
        return mapper.toResponse(application);
    }

    // ── updateStatus

    // FIX 1 — added candidateId parameter so @CacheEvict key is always available
    @Transactional
    @CacheEvict(value = CacheConfig.CANDIDATE_APPS_CACHE, key = "#candidateId")
    public ApplicationResponse updateStatus(UUID applicationId,
                                            UpdateStatusRequest req,
                                            UUID changedBy,
                                            UUID candidateId) { // FIX 1
        Application app = findApplicationById(applicationId);
        ApplicationStatus current = app.getStatus();
        ApplicationStatus next    = req.status();

        if (!current.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        app.setStatus(next);
        applicationRepository.save(app);

        recordHistory(app, current, next, req.reason(), changedBy);
        eventPublisher.publishStatusChanged(app, current, next, req.reason());

        log.info("Application {} transitioned: {} → {}", applicationId, current, next);
        return mapper.toResponse(app);
    }

    // ── withdraw

    // FIX 2 + FIX 3 — inlined logic, no self-invocation, correct cache eviction
    @Transactional
    @CacheEvict(value = CacheConfig.CANDIDATE_APPS_CACHE, key = "#candidateId")
    public ApplicationResponse withdraw(UUID applicationId, UUID candidateId) {
        Application app = findApplicationById(applicationId);

        if (!app.getCandidateId().equals(candidateId)) {
            throw new UnauthorizedApplicationAccessException(applicationId);
        }

        ApplicationStatus current = app.getStatus();
        ApplicationStatus next    = ApplicationStatus.WITHDRAWN;

        if (!current.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        app.setStatus(next);
        applicationRepository.save(app);

        recordHistory(app, current, next, "Withdrawn by candidate", candidateId);
        eventPublisher.publishStatusChanged(app, current, next, "Withdrawn by candidate");

        log.info("Application {} withdrawn by candidate: {}", applicationId, candidateId);
        return mapper.toResponse(app);
    }

    // ── queries

    // FIX 5 — cache a List, not Page (Page is not deserializable from Redis)
    @Cacheable(value = CacheConfig.CANDIDATE_APPS_CACHE, key = "#candidateId")
    public List<ApplicationResponse> findByCandidateIdCached(UUID candidateId) {
        return applicationRepository
                .findByCandidateIdOrderByAppliedAtDesc(candidateId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public Page<ApplicationResponse> findByCandidateId(UUID candidateId, Pageable pageable) {
        List<ApplicationResponse> all = findByCandidateIdCached(candidateId);
        int start = (int) pageable.getOffset();
        int end   = Math.min(start + pageable.getPageSize(), all.size());
        List<ApplicationResponse> slice = (start >= all.size())
                ? List.of()
                : all.subList(start, end);
        return new PageImpl<>(slice, pageable, all.size());
    }

    public Page<ApplicationResponse> findByJobId(UUID jobId, Pageable pageable) {
        return applicationRepository.findByJobId(jobId, pageable)
                .map(mapper::toResponse);
    }

    public ApplicationResponse findById(UUID applicationId) {
        return mapper.toResponse(findApplicationById(applicationId));
    }

    // ── helpers

    private Application findApplicationById(UUID id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private void recordHistory(Application app,
                               ApplicationStatus from,
                               ApplicationStatus to,
                               String reason,
                               UUID changedBy) {
        ApplicationStatusHistory history = ApplicationStatusHistory.builder()
                .application(app)
                .fromStatus(from)
                .toStatus(to)
                .reason(reason)
                .changedBy(changedBy)
                .build();
        historyRepository.save(history);
    }
}
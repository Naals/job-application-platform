package com.project.jobpostingservice.controller;


import com.project.jobpostingservice.dto.request.CreateJobRequest;
import com.project.jobpostingservice.dto.request.UpdateJobRequest;
import com.project.jobpostingservice.dto.response.JobResponse;
import com.project.jobpostingservice.search.JobDocument;
import com.project.jobpostingservice.search.JobSearchRequest;
import com.project.jobpostingservice.search.JobSearchService;
import com.project.jobpostingservice.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
@Tag(name = "Jobs")
public class JobController {

    private final JobService       jobService;
    private final JobSearchService searchService;

    @PostMapping
    @Operation(summary = "Create a new job (employer only)")
    public ResponseEntity<JobResponse> create(
            @Valid @RequestBody CreateJobRequest request,
            @RequestHeader("X-User-Id") UUID employerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.create(request, employerId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job by ID")
    public ResponseEntity<JobResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.findById(id));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all jobs posted by the authenticated employer")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            @RequestHeader("X-User-Id") UUID employerId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return ResponseEntity.ok(jobService.findByEmployer(employerId, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update job details (employer only, DRAFT status)")
    public ResponseEntity<JobResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateJobRequest request,
            @RequestHeader("X-User-Id") UUID employerId) {
        return ResponseEntity.ok(jobService.update(id, request, employerId));
    }

    @PatchMapping("/{id}/publish")
    @Operation(summary = "Publish a draft job")
    public ResponseEntity<JobResponse> publish(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID employerId) {
        return ResponseEntity.ok(jobService.publish(id, employerId));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close a published job")
    public ResponseEntity<JobResponse> close(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID employerId) {
        return ResponseEntity.ok(jobService.close(id, employerId));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job (DRAFT only)")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID employerId) {
        jobService.delete(id, employerId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Full-text job search (public)")
    public ResponseEntity<Page<JobDocument>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String experienceLevel,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {

        JobSearchRequest req = new JobSearchRequest(
                keyword, location, jobType,
                experienceLevel, remote, salaryMin, salaryMax);
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(searchService.search(req, pageable));
    }
}

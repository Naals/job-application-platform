package com.project.applicationservice.controller;

import com.project.applicationservice.domain.repository.ApplicationStatusHistoryRepository;
import com.project.applicationservice.dto.request.ApplyRequest;
import com.project.applicationservice.dto.request.UpdateStatusRequest;
import com.project.applicationservice.dto.response.ApplicationResponse;
import com.project.applicationservice.dto.response.StatusHistoryResponse;
import com.project.applicationservice.mapper.ApplicationMapper;
import com.project.applicationservice.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Tag(name = "Applications")
public class ApplicationController {

    private final ApplicationService                 applicationService;
    private final ApplicationStatusHistoryRepository historyRepository;
    private final ApplicationMapper                  mapper;

    @PostMapping
    @Operation(summary = "Apply to a job")
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplyRequest request,
            @RequestHeader("X-User-Id") UUID candidateId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.apply(request, candidateId));
    }

    @GetMapping("/my")
    @Operation(summary = "Get all my applications")
    public ResponseEntity<Page<ApplicationResponse>> myApplications(
            @RequestHeader("X-User-Id") UUID candidateId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appliedAt").descending());
        return ResponseEntity.ok(
                applicationService.findByCandidateId(candidateId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID")
    public ResponseEntity<ApplicationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.findById(id));
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get status transition history")
    public ResponseEntity<List<StatusHistoryResponse>> getHistory(
            @PathVariable UUID id) {
        List<StatusHistoryResponse> history = historyRepository
                .findByApplicationIdOrderByChangedAtAsc(id)
                .stream()
                .map(mapper::toHistoryResponse)
                .toList();
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update application status (employer only)")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @RequestHeader("X-User-Id") UUID changedBy) {

        UUID candidateId = applicationService.findById(id).candidateId();
        return ResponseEntity.ok(
                applicationService.updateStatus(id, request, changedBy, candidateId));
    }

    @PostMapping("/{id}/withdraw")
    @Operation(summary = "Withdraw my application")
    public ResponseEntity<ApplicationResponse> withdraw(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID candidateId) {
        return ResponseEntity.ok(applicationService.withdraw(id, candidateId));
    }

    @GetMapping("/job/{jobId}")
    @Operation(summary = "Get all applications for a job (employer only)")
    public ResponseEntity<Page<ApplicationResponse>> getByJob(
            @PathVariable UUID jobId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("appliedAt").descending());
        return ResponseEntity.ok(applicationService.findByJobId(jobId, pageable));
    }
}

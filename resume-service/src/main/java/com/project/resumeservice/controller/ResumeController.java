package com.project.resumeservice.controller;

import com.project.resumeservice.dto.response.ResumeResponse;
import com.project.resumeservice.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Tag(name = "Resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a resume (PDF or DOCX, max 10 MB)")
    public ResponseEntity<ResumeResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestHeader("X-User-Id") UUID candidateId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(resumeService.upload(file, candidateId));
    }

    @GetMapping("/my")
    @Operation(summary = "List all resumes uploaded by the authenticated candidate")
    public ResponseEntity<List<ResumeResponse>> myResumes(
            @RequestHeader("X-User-Id") UUID candidateId) {
        return ResponseEntity.ok(resumeService.findByCandidate(candidateId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get resume metadata by ID")
    public ResponseEntity<ResumeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(resumeService.findById(id));
    }

    @GetMapping("/{id}/download-url")
    @Operation(summary = "Get a presigned download URL (valid 7 days)")
    public ResponseEntity<Map<String, String>> downloadUrl(
            @PathVariable UUID id) {
        String url = resumeService.generateDownloadUrl(id);
        return ResponseEntity.ok(Map.of(
                "resumeId",    id.toString(),
                "downloadUrl", url
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a resume (removes from MinIO + DB)")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") UUID candidateId) {
        resumeService.delete(id, candidateId);
        return ResponseEntity.noContent().build();
    }
}
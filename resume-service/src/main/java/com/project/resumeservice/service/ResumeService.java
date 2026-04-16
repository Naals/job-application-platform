package com.project.resumeservice.service;

import com.project.resumeservice.domain.entity.Resume;
import com.project.resumeservice.domain.repository.ResumeRepository;
import com.project.resumeservice.dto.response.ResumeResponse;
import com.project.resumeservice.exception.InvalidFileTypeException;
import com.project.resumeservice.exception.ResumeNotFoundException;
import com.project.resumeservice.exception.FileSizeLimitException;
import com.project.resumeservice.kafka.ResumeEventPublisher;
import com.project.resumeservice.mapper.ResumeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository    resumeRepository;
    private final MinioService        minioService;
    private final TikaParserService   tikaParserService;
    private final ResumeEventPublisher eventPublisher;
    private final ResumeMapper        mapper;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "application/pdf",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    @Value("${resume.max-size-bytes:10485760}")
    private long maxSizeBytes;

    @Transactional
    public ResumeResponse upload(MultipartFile file, UUID candidateId) {

        // ── Validation ────────────────────────────────────
        if (file.isEmpty()) {
            throw new InvalidFileTypeException("File must not be empty");
        }
        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException(
                    "Only PDF and DOCX files are accepted. Got: " + file.getContentType());
        }
        if (file.getSize() > maxSizeBytes) {
            throw new FileSizeLimitException(file.getSize(), maxSizeBytes);
        }

        // ── Build object key ──────────────────────────────
        String ext       = getExtension(file.getOriginalFilename());
        String objectKey = "candidates/" + candidateId + "/" + UUID.randomUUID() + ext;

        // ── Upload to MinIO ───────────────────────────────
        minioService.uploadFile(objectKey, file);

        // ── Deactivate previous resume + persist new ─────
        resumeRepository.deactivateAllForCandidate(candidateId);

        Resume resume = Resume.builder()
                .candidateId(candidateId)
                .originalFileName(file.getOriginalFilename())
                .objectKey(objectKey)
                .contentType(file.getContentType())
                .fileSizeBytes(file.getSize())
                .isActive(true)
                .build();

        resume = resumeRepository.save(resume);
        log.info("Resume uploaded: id={} candidate={}", resume.getId(), candidateId);

        // ── Async Tika parse ──────────────────────────────
        tikaParserService.parseAsync(resume.getId());

        // ── Notify via Kafka ──────────────────────────────
        eventPublisher.publishResumeUploaded(resume);

        return mapper.toResponse(resume);
    }

    public ResumeResponse findById(UUID id) {
        return mapper.toResponse(getResumeById(id));
    }

    public List<ResumeResponse> findByCandidate(UUID candidateId) {
        return resumeRepository
                .findByCandidateIdOrderByUploadedAtDesc(candidateId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public String generateDownloadUrl(UUID id) {
        Resume resume = getResumeById(id);
        return minioService.generatePresignedUrl(resume.getObjectKey());
    }

    @Transactional
    public void delete(UUID id, UUID candidateId) {
        Resume resume = getResumeById(id);

        if (!resume.getCandidateId().equals(candidateId)) {
            throw new ResumeNotFoundException(id);  // don't leak existence to other users
        }

        minioService.deleteFile(resume.getObjectKey());
        resumeRepository.delete(resume);
        log.info("Resume deleted: id={}", id);
    }

    // ── Helpers ──────────────────────────────────────────

    private Resume getResumeById(UUID id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResumeNotFoundException(id));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf('.'));
    }
}

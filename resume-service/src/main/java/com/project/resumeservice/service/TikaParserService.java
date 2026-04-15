package com.project.resumeservice.service;

import com.project.resumeservice.domain.entity.Resume;
import com.project.resumeservice.domain.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TikaParserService {

    private final ResumeRepository resumeRepository;
    private final MinioService     minioService;

    // Single shared Tika instance is thread-safe
    private static final Tika TIKA = new Tika();

    /**
     * Runs asynchronously after the file has been persisted to MinIO.
     * Updates the resume row with extracted text and final parse status.
     */
    @Async
    @Transactional
    public void parseAsync(UUID resumeId) {
        Resume resume = resumeRepository.findById(resumeId).orElse(null);
        if (resume == null) {
            log.warn("TikaParserService: resume not found for id={}", resumeId);
            return;
        }

        resume.setParseStatus(Resume.ParseStatus.PROCESSING);
        resumeRepository.save(resume);

        try (InputStream is = minioService.downloadFile(resume.getObjectKey())) {

            String text = TIKA.parseToString(is);
            resume.setExtractedText(text);
            resume.setParseStatus(Resume.ParseStatus.COMPLETED);
            resumeRepository.save(resume);
            log.info("Tika parsed resume {} — {} chars extracted",
                    resumeId, text.length());

        } catch (Exception ex) {
            resume.setParseStatus(Resume.ParseStatus.FAILED);
            resumeRepository.save(resume);
            log.error("Tika parse failed for resume {}: {}", resumeId, ex.getMessage());
        }
    }
}
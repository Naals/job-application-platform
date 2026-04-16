package com.project.resumeservice;

import com.project.resumeservice.domain.entity.Resume;
import com.project.resumeservice.domain.repository.ResumeRepository;
import com.project.resumeservice.exception.FileSizeLimitException;
import com.project.resumeservice.exception.InvalidFileTypeException;
import com.project.resumeservice.kafka.ResumeEventPublisher;
import com.project.resumeservice.mapper.ResumeMapper;
import com.project.resumeservice.service.MinioService;
import com.project.resumeservice.service.ResumeService;
import com.project.resumeservice.service.TikaParserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock ResumeRepository    resumeRepository;
    @Mock MinioService minioService;
    @Mock TikaParserService tikaParserService;
    @Mock ResumeEventPublisher eventPublisher;
    @Mock ResumeMapper        mapper;

    @InjectMocks ResumeService resumeService;

    @Test
    void upload_validPdf_savesAndPublishes() {
        ReflectionTestUtils.setField(resumeService, "maxSizeBytes", 10_485_760L);
        UUID candidateId = UUID.randomUUID();

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf",
                new byte[1024]);

        Resume saved = Resume.builder()
                .id(UUID.randomUUID()).candidateId(candidateId)
                .originalFileName("cv.pdf").objectKey("candidates/x/y.pdf")
                .contentType("application/pdf").fileSizeBytes(1024L)
                .isActive(true).build();

        when(resumeRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(null);

        resumeService.upload(file, candidateId);

        verify(minioService).uploadFile(any(), eq(file));
        verify(resumeRepository).deactivateAllForCandidate(candidateId);
        verify(resumeRepository).save(any());
        verify(tikaParserService).parseAsync(saved.getId());
        verify(eventPublisher).publishResumeUploaded(saved);
    }

    @Test
    void upload_invalidContentType_throws() {
        ReflectionTestUtils.setField(resumeService, "maxSizeBytes", 10_485_760L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "image.png", "image/png", new byte[100]);

        assertThatThrownBy(() -> resumeService.upload(file, UUID.randomUUID()))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessageContaining("image/png");
    }

    @Test
    void upload_fileTooLarge_throws() {
        ReflectionTestUtils.setField(resumeService, "maxSizeBytes", 1024L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "huge.pdf", "application/pdf",
                new byte[2048]);

        assertThatThrownBy(() -> resumeService.upload(file, UUID.randomUUID()))
                .isInstanceOf(FileSizeLimitException.class);
    }

    @Test
    void upload_emptyFile_throws() {
        ReflectionTestUtils.setField(resumeService, "maxSizeBytes", 10_485_760L);
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() -> resumeService.upload(file, UUID.randomUUID()))
                .isInstanceOf(InvalidFileTypeException.class)
                .hasMessageContaining("empty");
    }
}

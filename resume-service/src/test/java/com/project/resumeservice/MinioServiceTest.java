package com.project.resumeservice;

import com.project.resumeservice.config.MinioProperties;
import com.project.resumeservice.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock MinioClient     minioClient;
    @Mock MinioProperties props;
    @InjectMocks MinioService minioService;

    @Test
    void uploadFile_callsPutObject() throws Exception {
        when(props.getBucket()).thenReturn("resumes");
        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf", "PDF content".getBytes());

        minioService.uploadFile("candidates/uuid/cv.pdf", file);

        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    void deleteFile_callsRemoveObject() throws Exception {
        when(props.getBucket()).thenReturn("resumes");

        minioService.deleteFile("candidates/uuid/cv.pdf");

        verify(minioClient).removeObject(any(RemoveObjectArgs.class));
    }

    @Test
    void generatePresignedUrl_minioThrows_wrapsException() throws Exception {
        when(props.getBucket()).thenReturn("resumes");
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class)))
                .thenThrow(new RuntimeException("MinIO down"));

        assertThatThrownBy(() -> minioService.generatePresignedUrl("some/key"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Presigned URL generation failed");
    }
}

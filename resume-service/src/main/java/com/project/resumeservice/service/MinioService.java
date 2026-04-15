package com.project.resumeservice.service;

import com.project.resumeservice.config.MinioProperties;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient     minioClient;
    private final MinioProperties props;

    @Value("${resume.presigned-url-expiry-days:7}")
    private int presignedExpiryDays;



    public void uploadFile(String objectKey,
                           MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .stream(is, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());

            log.info("Uploaded file to MinIO: bucket={} key={}",
                    props.getBucket(), objectKey);

        } catch (Exception ex) {
            log.error("MinIO upload failed for key {}: {}", objectKey, ex.getMessage());
            throw new RuntimeException("File upload failed", ex);
        }
    }


    public InputStream downloadFile(String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            log.error("MinIO download failed for key {}: {}", objectKey, ex.getMessage());
            throw new RuntimeException("File download failed", ex);
        }
    }


    public void deleteFile(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(props.getBucket())
                    .object(objectKey)
                    .build());

            log.info("Deleted file from MinIO: key={}", objectKey);

        } catch (Exception ex) {
            log.error("MinIO delete failed for key {}: {}", objectKey, ex.getMessage());
            throw new RuntimeException("File deletion failed", ex);
        }
    }


    public String generatePresignedUrl(String objectKey) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(props.getBucket())
                            .object(objectKey)
                            .method(Method.GET)
                            .expiry(presignedExpiryDays, TimeUnit.DAYS)
                            .build());
        } catch (Exception ex) {
            log.error("Failed to generate presigned URL for key {}: {}",
                    objectKey, ex.getMessage());
            throw new RuntimeException("Presigned URL generation failed", ex);
        }
    }
}

package com.project.resumeservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        schema = "resumes",
        name   = "resumes",
        indexes = {
                @Index(name = "idx_resume_candidate_id",  columnList = "candidate_id"),
                @Index(name = "idx_resume_parse_status",  columnList = "parse_status"),
                @Index(name = "idx_resume_is_active",     columnList = "is_active")
        }
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID candidateId;

    /** Original filename supplied by the uploader */
    @Column(nullable = false, length = 255)
    private String originalFileName;


    @Column(nullable = false, length = 512)
    private String objectKey;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ParseStatus parseStatus = ParseStatus.PENDING;

    /** Text extracted by Apache Tika — used for keyword matching */
    @Column(columnDefinition = "TEXT")
    private String extractedText;

    /** Only one resume is active per candidate at any time */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum ParseStatus {
        PENDING,    // just uploaded, not yet parsed
        PROCESSING, // Tika is running
        COMPLETED,  // text extracted successfully
        FAILED      // Tika threw an error
    }
}

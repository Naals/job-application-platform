package com.project.jobpostingservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "jobs", indexes = {
        @Index(name = "idx_jobs_employer_id", columnList = "employer_id"),
        @Index(name = "idx_jobs_status",      columnList = "status"),
        @Index(name = "idx_jobs_location",    columnList = "location")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String company;

    @Column(nullable = false)
    private UUID employerId;

    @Column(length = 100)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobType jobType = JobType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExperienceLevel experienceLevel = ExperienceLevel.MID;

    private BigDecimal salaryMin;
    private BigDecimal salaryMax;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Column(columnDefinition = "TEXT")
    private String requirements;

    @Column(columnDefinition = "TEXT")
    private String benefits;

    @Column(nullable = false)
    @Builder.Default
    private Boolean remote = false;

    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum JobType {
        FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP, FREELANCE
    }

    public enum JobStatus {
        DRAFT, PUBLISHED, CLOSED, ARCHIVED
    }

    public enum ExperienceLevel {
        JUNIOR, MID, SENIOR, LEAD, EXECUTIVE
    }
}

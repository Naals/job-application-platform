package com.project.applicationservice.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        schema = "applications",
        name = "applications",
        indexes = {
                @Index(name = "idx_app_candidate_id", columnList = "candidate_id"),
                @Index(name = "idx_app_job_id",       columnList = "job_id"),
                @Index(name = "idx_app_status",       columnList = "status")
        },
        uniqueConstraints = @UniqueConstraint(
                name = "uq_candidate_job",
                columnNames = {"candidate_id", "job_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false)
    private UUID candidateId;

    @Column(nullable = false)
    private UUID jobId;

    @Column(nullable = false, length = 150)
    private String jobTitle;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    private UUID resumeId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "application",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY)
    @Builder.Default
    private List<ApplicationStatusHistory> statusHistory = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}

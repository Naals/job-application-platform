package com.project.resumeservice.domain.repository;

import com.project.resumeservice.domain.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResumeRepository extends JpaRepository<Resume, UUID> {
    List<Resume> findByCandidateIdOrderByUploadedAtDesc(UUID candidateId);
    Optional<Resume> findByCandidateIdAndIsActiveTrue(UUID candidateId);

    @Modifying
    @Query("UPDATE Resume r SET r.isActive = false WHERE r.candidateId = :candidateId")
    void deactivateAllForCandidate(UUID candidateId);
}

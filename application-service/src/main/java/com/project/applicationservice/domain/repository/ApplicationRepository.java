package com.project.applicationservice.domain.repository;


import com.project.applicationservice.domain.entity.Application;
import com.project.applicationservice.domain.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
    Optional<Application> findByCandidateIdAndJobId(UUID candidateId, UUID jobId);
    boolean existsByCandidateIdAndJobId(UUID candidateId, UUID jobId);
    Page<Application> findByCandidateId(UUID candidateId, Pageable pageable);
    Page<Application> findByJobId(UUID jobId, Pageable pageable);
    List<Application> findByJobIdAndStatus(UUID jobId, ApplicationStatus status);
}

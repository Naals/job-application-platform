package com.project.jobpostingservice.domain.repository;


import com.project.jobpostingservice.domain.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobRepository extends JpaRepository<Job, UUID> {
    Page<Job> findByEmployerId(UUID employerId, Pageable pageable);
    Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);
    List<Job> findByEmployerIdAndStatus(UUID employerId, Job.JobStatus status);
    boolean existsByIdAndEmployerId(UUID id, UUID employerId);
}

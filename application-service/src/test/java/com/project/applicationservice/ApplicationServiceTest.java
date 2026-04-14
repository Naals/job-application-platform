package com.project.applicationservice;

import com.project.applicationservice.client.JobClient;
import com.project.applicationservice.client.dto.JobDto;
import com.project.applicationservice.domain.entity.Application;
import com.project.applicationservice.domain.entity.ApplicationStatus;
import com.project.applicationservice.domain.repository.ApplicationRepository;
import com.project.applicationservice.domain.repository.ApplicationStatusHistoryRepository;
import com.project.applicationservice.dto.request.ApplyRequest;
import com.project.applicationservice.dto.request.UpdateStatusRequest;
import com.project.applicationservice.exception.*;
import com.project.applicationservice.kafka.ApplicationEventPublisher;
import com.project.applicationservice.mapper.ApplicationMapper;
import com.project.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceTest {

    @Mock ApplicationRepository            applicationRepository;
    @Mock ApplicationStatusHistoryRepository historyRepository;
    @Mock ApplicationMapper                mapper;
    @Mock JobClient                        jobClient;
    @Mock ApplicationEventPublisher        eventPublisher;

    @InjectMocks ApplicationService applicationService;

    @Test
    void apply_success() {
        UUID candidateId = UUID.randomUUID();
        UUID jobId       = UUID.randomUUID();
        ApplyRequest req = new ApplyRequest(jobId, "Cover letter", null);

        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId))
                .thenReturn(false);
        when(jobClient.getJobById(jobId))
                .thenReturn(new JobDto(jobId, "Java Dev", "Acme", "PUBLISHED"));

        Application saved = Application.builder()
                .id(UUID.randomUUID()).candidateId(candidateId).jobId(jobId)
                .jobTitle("Java Dev").companyName("Acme")
                .status(ApplicationStatus.SUBMITTED).build();
        when(applicationRepository.save(any())).thenReturn(saved);
        when(mapper.toResponse(saved)).thenReturn(null);

        applicationService.apply(req, candidateId);

        verify(applicationRepository).save(any());
        verify(historyRepository).save(any());
        verify(eventPublisher).publishApplicationSubmitted(saved);
    }

    @Test
    void apply_duplicateApplication_throws() {
        UUID candidateId = UUID.randomUUID();
        UUID jobId       = UUID.randomUUID();
        ApplyRequest req = new ApplyRequest(jobId, null, null);

        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId))
                .thenReturn(true);

        assertThatThrownBy(() -> applicationService.apply(req, candidateId))
                .isInstanceOf(DuplicateApplicationException.class);
    }

    @Test
    void apply_jobNotPublished_throws() {
        UUID candidateId = UUID.randomUUID();
        UUID jobId       = UUID.randomUUID();
        ApplyRequest req = new ApplyRequest(jobId, null, null);

        when(applicationRepository.existsByCandidateIdAndJobId(candidateId, jobId))
                .thenReturn(false);
        when(jobClient.getJobById(jobId))
                .thenReturn(new JobDto(jobId, "Java Dev", "Acme", "CLOSED"));

        assertThatThrownBy(() -> applicationService.apply(req, candidateId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PUBLISHED");
    }

    @Test
    void updateStatus_validTransition_succeeds() {
        UUID appId   = UUID.randomUUID();
        UUID userId  = UUID.randomUUID();
        Application app = Application.builder()
                .id(appId).candidateId(UUID.randomUUID())
                .jobId(UUID.randomUUID()).jobTitle("Dev").companyName("X")
                .status(ApplicationStatus.SUBMITTED).build();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));
        when(applicationRepository.save(any())).thenReturn(app);
        when(mapper.toResponse(any())).thenReturn(null);

        applicationService.updateStatus(appId,
                new UpdateStatusRequest(ApplicationStatus.UNDER_REVIEW, "Reviewing"), userId);

        assertThat(app.getStatus()).isEqualTo(ApplicationStatus.UNDER_REVIEW);
        verify(eventPublisher).publishStatusChanged(
                any(), eq(ApplicationStatus.SUBMITTED),
                eq(ApplicationStatus.UNDER_REVIEW), any());
    }

    @Test
    void updateStatus_invalidTransition_throws() {
        UUID appId  = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Application app = Application.builder()
                .id(appId).candidateId(UUID.randomUUID())
                .jobId(UUID.randomUUID())
                .status(ApplicationStatus.ACCEPTED).build(); // terminal state

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> applicationService.updateStatus(appId,
                new UpdateStatusRequest(ApplicationStatus.SUBMITTED, "revert"), userId))
                .isInstanceOf(InvalidStatusTransitionException.class);
    }

    @Test
    void withdraw_wrongCandidate_throws() {
        UUID appId       = UUID.randomUUID();
        UUID realOwner   = UUID.randomUUID();
        UUID otherUser   = UUID.randomUUID();
        Application app  = Application.builder()
                .id(appId).candidateId(realOwner)
                .jobId(UUID.randomUUID())
                .status(ApplicationStatus.SUBMITTED).build();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(app));

        assertThatThrownBy(() -> applicationService.withdraw(appId, otherUser))
                .isInstanceOf(UnauthorizedApplicationAccessException.class);
    }
}

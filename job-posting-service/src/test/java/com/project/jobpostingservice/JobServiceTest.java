package com.project.jobpostingservice;

import com.project.jobpostingservice.domain.entity.Job;
import com.project.jobpostingservice.domain.repository.JobRepository;
import com.project.jobpostingservice.dto.request.CreateJobRequest;
import com.project.jobpostingservice.dto.response.JobResponse;
import com.project.jobpostingservice.exception.JobNotFoundException;
import com.project.jobpostingservice.exception.UnauthorizedJobAccessException;
import com.project.jobpostingservice.kafka.JobEventPublisher;
import com.project.jobpostingservice.mapper.JobMapper;
import com.project.jobpostingservice.search.JobSearchService;
import com.project.jobpostingservice.service.JobService;
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
class JobServiceTest {

    @Mock
    JobRepository jobRepository;
    @Mock
    JobMapper jobMapper;
    @Mock
    JobSearchService searchService;
    @Mock
    JobEventPublisher eventPublisher;

    @InjectMocks
    JobService jobService;

    @Test
    void create_savesAndReturnsResponse() {
        UUID employerId = UUID.randomUUID();
        CreateJobRequest req = new CreateJobRequest(
                "Java Dev", "Description", "Acme", "Almaty",
                Job.JobType.FULL_TIME, Job.ExperienceLevel.SENIOR,
                null, null, "USD", null, null, false, null);

        Job savedJob = Job.builder().id(UUID.randomUUID())
                .title("Java Dev").company("Acme")
                .employerId(employerId).status(Job.JobStatus.DRAFT).build();
        JobResponse fakeResponse = new JobResponse(savedJob.getId(), "Java Dev",
                "Description", "Acme", employerId, "Almaty",
                Job.JobType.FULL_TIME, Job.JobStatus.DRAFT,
                Job.ExperienceLevel.SENIOR, null, null, "USD",
                null, null, false, null, null, null);

        when(jobMapper.toEntity(req)).thenReturn(savedJob);
        when(jobRepository.save(any())).thenReturn(savedJob);
        when(jobMapper.toResponse(savedJob)).thenReturn(fakeResponse);

        JobResponse result = jobService.create(req, employerId);

        assertThat(result.title()).isEqualTo("Java Dev");
        verify(jobRepository).save(any());
    }

    @Test
    void publish_changeStatusAndIndexesAndPublishesEvent() {
        UUID jobId     = UUID.randomUUID();
        UUID ownerId   = UUID.randomUUID();
        Job draftJob   = Job.builder().id(jobId).employerId(ownerId)
                .status(Job.JobStatus.DRAFT).title("Dev").company("X").build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(draftJob));
        when(jobRepository.save(any())).thenReturn(draftJob);
        when(jobMapper.toDocument(any())).thenReturn(null);
        when(jobMapper.toResponse(any())).thenReturn(mock(JobResponse.class));

        jobService.publish(jobId, ownerId);

        assertThat(draftJob.getStatus()).isEqualTo(Job.JobStatus.PUBLISHED);
        verify(searchService).indexJob(any());
        verify(eventPublisher).publishJobPosted(draftJob);
    }

    @Test
    void publish_wrongOwner_throwsForbidden() {
        UUID jobId   = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        Job job      = Job.builder().id(jobId).employerId(ownerId)
                .status(Job.JobStatus.DRAFT).build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.publish(jobId, otherId))
                .isInstanceOf(UnauthorizedJobAccessException.class);
    }

    @Test
    void findById_notFound_throwsNotFoundException() {
        UUID id = UUID.randomUUID();
        when(jobRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jobService.findById(id))
                .isInstanceOf(JobNotFoundException.class);
    }

    @Test
    void close_alreadyClosed_throwsIllegalState() {
        UUID jobId   = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Job job      = Job.builder().id(jobId).employerId(ownerId)
                .status(Job.JobStatus.CLOSED).build();

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> jobService.close(jobId, ownerId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("PUBLISHED");
    }
}

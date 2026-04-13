package com.project.jobpostingservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.jobpostingservice.controller.JobController;
import com.project.jobpostingservice.dto.request.CreateJobRequest;
import com.project.jobpostingservice.dto.response.JobResponse;
import com.project.jobpostingservice.domain.entity.Job;
import com.project.jobpostingservice.service.JobService;
import com.project.jobpostingservice.search.JobSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired MockMvc      mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean  JobService      jobService;
    @MockBean  JobSearchService searchService;

    @Test
    void createJob_returns201() throws Exception {
        UUID employerId = UUID.randomUUID();
        UUID jobId      = UUID.randomUUID();

        CreateJobRequest req = new CreateJobRequest(
                "Java Dev", "Great role", "Acme", "Almaty",
                Job.JobType.FULL_TIME, Job.ExperienceLevel.SENIOR,
                null, null, "USD", null, null, false, null);

        JobResponse resp = new JobResponse(
                jobId, "Java Dev", "Great role", "Acme", employerId,
                "Almaty", Job.JobType.FULL_TIME, Job.JobStatus.DRAFT,
                Job.ExperienceLevel.SENIOR, null, null, "USD",
                null, null, false, null, null, null);

        when(jobService.create(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/jobs")
                        .header("X-User-Id", employerId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Java Dev"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void createJob_missingTitle_returns400() throws Exception {
        CreateJobRequest req = new CreateJobRequest(
                "", "desc", "Acme", null,
                Job.JobType.FULL_TIME, Job.ExperienceLevel.MID,
                null, null, "USD", null, null, false, null);

        mockMvc.perform(post("/api/v1/jobs")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}

package com.project.applicationservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.applicationservice.controller.ApplicationController;
import com.project.applicationservice.domain.entity.ApplicationStatus;
import com.project.applicationservice.dto.request.ApplyRequest;
import com.project.applicationservice.dto.response.ApplicationResponse;
import com.project.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired MockMvc       mockMvc;
    @Autowired ObjectMapper  objectMapper;
    @MockBean  ApplicationService                 applicationService;

    @Test
    void apply_returns201() throws Exception {
        UUID candidateId = UUID.randomUUID();
        UUID jobId       = UUID.randomUUID();
        UUID appId       = UUID.randomUUID();

        ApplyRequest req = new ApplyRequest(jobId, "Great opportunity", null);

        ApplicationResponse resp = new ApplicationResponse(
                appId, candidateId, jobId, "Java Dev", "Acme",
                ApplicationStatus.SUBMITTED, "Great opportunity",
                null, null, LocalDateTime.now(), LocalDateTime.now());

        when(applicationService.apply(any(), any())).thenReturn(resp);

        mockMvc.perform(post("/api/v1/applications")
                        .header("X-User-Id", candidateId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.jobTitle").value("Java Dev"));
    }

    @Test
    void apply_missingJobId_returns400() throws Exception {
        String invalidBody = """
                { "coverLetter": "hi" }
                """;
        mockMvc.perform(post("/api/v1/applications")
                        .header("X-User-Id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest());
    }
}

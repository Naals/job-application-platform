package com.project.resumeservice;


import com.project.resumeservice.controller.ResumeController;
import com.project.resumeservice.dto.response.ResumeResponse;
import com.project.resumeservice.domain.entity.Resume;
import com.project.resumeservice.service.ResumeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean  ResumeService resumeService;

    @Test
    void upload_validFile_returns201() throws Exception {
        UUID candidateId = UUID.randomUUID();
        UUID resumeId    = UUID.randomUUID();

        ResumeResponse response = new ResumeResponse(
                resumeId, candidateId, "cv.pdf",
                "application/pdf", 1024L,
                Resume.ParseStatus.PENDING, true,
                LocalDateTime.now());

        when(resumeService.upload(any(), any())).thenReturn(response);

        MockMultipartFile file = new MockMultipartFile(
                "file", "cv.pdf", "application/pdf",
                "PDF content".getBytes());

        mockMvc.perform(multipart("/api/v1/resumes/upload")
                        .file(file)
                        .header("X-User-Id", candidateId.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.originalFileName").value("cv.pdf"))
                .andExpect(jsonPath("$.parseStatus").value("PENDING"))
                .andExpect(jsonPath("$.isActive").value(true));
    }
}

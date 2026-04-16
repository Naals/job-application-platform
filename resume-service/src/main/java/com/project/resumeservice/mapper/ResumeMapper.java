package com.project.resumeservice.mapper;

import com.project.resumeservice.domain.entity.Resume;
import com.project.resumeservice.dto.response.ResumeResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResumeMapper {
    ResumeResponse toResponse(Resume resume);
}

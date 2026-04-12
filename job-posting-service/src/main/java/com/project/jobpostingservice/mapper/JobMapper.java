package com.project.jobpostingservice.mapper;


import com.project.jobpostingservice.domain.entity.Job;
import com.project.jobpostingservice.dto.request.CreateJobRequest;
import com.project.jobpostingservice.dto.request.UpdateJobRequest;
import com.project.jobpostingservice.dto.response.JobResponse;
import com.project.jobpostingservice.search.JobDocument;
import org.mapstruct.*;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobMapper {

    Job toEntity(CreateJobRequest request);

    JobResponse toResponse(Job job);

    @Mapping(target = "jobType",         expression = "java(job.getJobType().name())")
    @Mapping(target = "status",          expression = "java(job.getStatus().name())")
    @Mapping(target = "experienceLevel", expression = "java(job.getExperienceLevel().name())")
    @Mapping(target = "id",              expression = "java(job.getId().toString())")
    JobDocument toDocument(Job job);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(UpdateJobRequest request, @MappingTarget Job job);
}

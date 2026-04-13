package com.project.applicationservice.mapper;


import com.project.applicationservice.domain.entity.Application;
import com.project.applicationservice.domain.entity.ApplicationStatusHistory;
import com.project.applicationservice.dto.response.ApplicationResponse;
import com.project.applicationservice.dto.response.StatusHistoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplicationMapper {
    ApplicationResponse toResponse(Application application);
    StatusHistoryResponse toHistoryResponse(ApplicationStatusHistory history);
}

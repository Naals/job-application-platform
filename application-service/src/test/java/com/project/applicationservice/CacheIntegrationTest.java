package com.project.applicationservice;


import com.project.applicationservice.config.CacheConfig;
import com.project.applicationservice.domain.repository.ApplicationRepository;
import com.project.applicationservice.mapper.ApplicationMapper;
import com.project.applicationservice.service.ApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class CacheIntegrationTest {

    @Autowired CacheManager      cacheManager;
    @MockBean  ApplicationRepository applicationRepository;
    @MockBean  ApplicationMapper mapper;
    @Autowired ApplicationService applicationService;

    @Test
    void findByCandidateId_secondCall_hitsCache() {
        UUID candidateId = UUID.randomUUID();
        when(applicationRepository.findByCandidateId(eq(candidateId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(mapper.toResponse(any())).thenReturn(null);

        applicationService.findByCandidateId(candidateId, Pageable.unpaged());
        applicationService.findByCandidateId(candidateId, Pageable.unpaged());

        // Repository called only once — second hit served from cache
        verify(applicationRepository, times(1))
                .findByCandidateId(eq(candidateId), any(Pageable.class));

        assertThat(cacheManager.getCache(CacheConfig.CANDIDATE_APPS_CACHE)).isNotNull();
    }
}

package com.project.applicationservice.client.dto;

import java.util.UUID;

public record JobDto(
        UUID   id,
        String title,
        String company,
        String status
) {}

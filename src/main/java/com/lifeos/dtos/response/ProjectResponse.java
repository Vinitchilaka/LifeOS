package com.lifeos.dtos.response;

import java.time.LocalDateTime;

public record ProjectResponse(
    Long id,
    String name,
    String description,
    LocalDateTime createdAt
) {}

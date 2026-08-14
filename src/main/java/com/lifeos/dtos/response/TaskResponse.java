package com.lifeos.dtos.response;

import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    LocalDateTime dueDate,
    String priority,
    String status,
    Double estimatedEffort,
    Long projectId,
    String projectName,
    Long goalId,
    String goalTitle,
    LocalDateTime createdAt
) {}

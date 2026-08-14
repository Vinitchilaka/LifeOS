package com.lifeos.dtos.response;

import java.time.LocalDateTime;

public record GoalResponse(
    Long id,
    String title,
    String description,
    LocalDateTime targetDate,
    Double progressPercentage,
    String status,
    LocalDateTime createdAt
) {}

package com.lifeos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record GoalRequest(
    @NotBlank(message = "Goal title cannot be blank")
    @Size(min = 2, max = 150, message = "Goal title must be between 2 and 150 characters")
    String title,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    LocalDateTime targetDate,

    @NotBlank(message = "Status is required")
    String status
) {}

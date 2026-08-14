package com.lifeos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TaskRequest(
    @NotBlank(message = "Task title cannot be blank")
    @Size(min = 2, max = 150, message = "Task title must be between 2 and 150 characters")
    String title,

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    String description,

    LocalDateTime dueDate,

    @NotBlank(message = "Priority is required")
    String priority,

    @NotBlank(message = "Status is required")
    String status,

    Double estimatedEffort,

    Long projectId,
    Long goalId
) {}

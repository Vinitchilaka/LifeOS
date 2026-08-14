package com.lifeos.dtos.response;

public record PrioritizedTaskItem(
    Long taskId,
    Integer priorityOrder,
    String reasoning
) {}

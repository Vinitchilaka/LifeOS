package com.lifeos.dtos.response;

import java.util.List;

public record PrioritizedTasksResponse(
    List<PrioritizedTaskItem> prioritizedTasks
) {}

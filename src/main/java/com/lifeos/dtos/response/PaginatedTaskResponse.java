package com.lifeos.dtos.response;

import java.util.List;

public record PaginatedTaskResponse(
    List<TaskResponse> content,
    int pageNo,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {}

package com.lifeos.dtos.response;

import java.util.List;

public record PaginatedUserResponse(
    List<UserResponseDTO> content,
    int pageNo,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean last
) {}

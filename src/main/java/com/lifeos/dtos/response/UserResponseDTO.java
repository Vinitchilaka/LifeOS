package com.lifeos.dtos.response;
import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String mobileNo,
        LocalDateTime createdAt
) {}

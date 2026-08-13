package com.lifeos.dtos.response;

public record LoginResponse(
        String token,
        String type, // Will be "Bearer"
        long expiresInMs
) {}

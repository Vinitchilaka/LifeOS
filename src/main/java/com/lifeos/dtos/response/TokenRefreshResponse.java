package com.lifeos.dtos.response;

public record TokenRefreshResponse(
    String accessToken,
    String tokenType,
    long expiresInMs,
    String refreshToken
) {}

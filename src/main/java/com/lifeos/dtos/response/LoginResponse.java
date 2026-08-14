package com.lifeos.dtos.response;

public record LoginResponse(
                String token, // renamed to accessToken
                String type, // "Bearer"
                long expiresInMs, // 900000 ms (15 minutes)
                String refreshToken) {
}

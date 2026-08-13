package com.lifeos.dtos.common;

import java.time.LocalDateTime;

public record ErrorResponse(int status, String error, String message) {
    }

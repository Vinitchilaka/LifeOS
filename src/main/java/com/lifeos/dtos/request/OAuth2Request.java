package com.lifeos.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record OAuth2Request(
    @NotBlank(message = "Authorization code is required")
    String code,

    @NotBlank(message = "Redirect URI is required")
    String redirectUri
) {}

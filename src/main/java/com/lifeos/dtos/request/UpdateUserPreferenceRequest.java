package com.lifeos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateUserPreferenceRequest(
    @NotBlank(message = "Theme cannot be blank")
    String theme,

    @NotBlank(message = "Language cannot be blank")
    String language,

    @NotBlank(message = "Timezone cannot be blank")
    String timezone,

    @NotNull(message = "Email notification setting is required")
    Boolean emailNotifications
) {}

package com.lifeos.dtos.response;

public record UserPreferencesResponse(
    String theme,
    String language,
    String timezone,
    Boolean emailNotifications
) {}

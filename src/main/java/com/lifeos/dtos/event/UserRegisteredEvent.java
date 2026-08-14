package com.lifeos.dtos.event;

public record UserRegisteredEvent(
    String email,
    String firstName,
    String lastName
) {}

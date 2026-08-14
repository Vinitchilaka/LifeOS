package com.lifeos.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateUserProfileRequest(
    @NotBlank(message = "First name cannot be blank")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    String lastName,

    @Pattern(regexp = "^$|[0-9]{10}", message = "Mobile number must be a valid 10-digit number")
    String mobileNo
) {}

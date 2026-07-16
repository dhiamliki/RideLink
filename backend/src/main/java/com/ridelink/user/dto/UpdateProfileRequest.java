package com.ridelink.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank
        @Size(min = 1, max = 100, message = "displayName must be 1-100 characters")
        String displayName,

        @Size(max = 500, message = "bio must be at most 500 characters")
        String bio) {
}

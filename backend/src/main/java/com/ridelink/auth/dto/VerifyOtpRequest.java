package com.ridelink.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotBlank
        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "phoneNumber must be E.164, e.g. +21612345678")
        String phoneNumber,

        @NotBlank
        @Pattern(regexp = "^\\d{6}$", message = "code must be 6 digits")
        String code) {
}

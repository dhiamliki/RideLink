package com.ridelink.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

// devCode is populated ONLY under the "dev" profile; it is null (and omitted) otherwise.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OtpResponse(String message, String devCode) {
}

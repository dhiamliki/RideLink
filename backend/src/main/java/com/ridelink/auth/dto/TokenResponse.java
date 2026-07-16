package com.ridelink.auth.dto;

public record TokenResponse(String accessToken, String refreshToken, boolean isNewUser) {
}

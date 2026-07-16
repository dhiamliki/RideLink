package com.ridelink.user.dto;

import com.ridelink.user.User;
import java.util.UUID;

public record ProfileResponse(
        UUID id,
        String phoneNumber,
        String displayName,
        String photoUrl,
        String bio,
        boolean phoneVerified,
        boolean isProfileComplete) {

    public static ProfileResponse from(User user) {
        boolean complete = user.getDisplayName() != null && !user.getDisplayName().isBlank();
        return new ProfileResponse(
                user.getId(),
                user.getPhoneNumber(),
                user.getDisplayName(),
                user.getPhotoUrl(),
                user.getBio(),
                user.isPhoneVerified(),
                complete);
    }
}

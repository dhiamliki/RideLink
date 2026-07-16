package com.ridelink.ride.dto;

import com.ridelink.user.User;
import java.util.UUID;

// Public poster info surfaced with a ride (never phone number or other private fields).
public record UserSummary(UUID id, String displayName, String photoUrl) {

    public static UserSummary from(User user) {
        return new UserSummary(user.getId(), user.getDisplayName(), user.getPhotoUrl());
    }
}

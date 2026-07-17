package com.ridelink.safety.dto;

import com.ridelink.user.User;
import java.util.UUID;

// A user the current user has blocked, for the manage/unblock screen.
public record BlockedUserResponse(UUID id, String displayName, String photoUrl) {

    public static BlockedUserResponse from(User user) {
        return new BlockedUserResponse(user.getId(), user.getDisplayName(), user.getPhotoUrl());
    }
}

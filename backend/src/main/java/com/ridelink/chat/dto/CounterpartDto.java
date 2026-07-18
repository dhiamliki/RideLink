package com.ridelink.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.user.User;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CounterpartDto(UUID id, String displayName, String photoUrl) {

    public static CounterpartDto of(User u) {
        return new CounterpartDto(u.getId(), u.getDisplayName(), u.getPhotoUrl());
    }
}

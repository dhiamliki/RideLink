package com.ridelink.chat.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ReadRequest(@NotNull UUID conversationId) {
}

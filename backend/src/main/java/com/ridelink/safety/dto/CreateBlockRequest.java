package com.ridelink.safety.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBlockRequest(@NotNull UUID blockedUserId) {
}

package com.ridelink.chat.dto;

import java.time.Instant;
import java.util.UUID;

// Broadcast on /topic/conversations/{id}/read when a participant reads the thread, so the other
// side can flip its sent messages to "read" live.
public record ReadReceipt(UUID conversationId, UUID readerId, Instant readAt, int count) {
}

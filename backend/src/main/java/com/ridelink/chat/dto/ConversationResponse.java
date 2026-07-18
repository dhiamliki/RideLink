package com.ridelink.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.UUID;

// counterpart + last message + my unread count, for the conversations list.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConversationResponse(
        UUID id,
        CounterpartDto counterpart,
        MessageResponse lastMessage,
        long unreadCount,
        Instant createdAt) {
}

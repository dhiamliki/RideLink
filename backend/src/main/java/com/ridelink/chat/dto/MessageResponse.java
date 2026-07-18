package com.ridelink.chat.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.chat.Message;
import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record MessageResponse(
        UUID id,
        UUID conversationId,
        UUID senderId,
        String content,
        Instant sentAt,
        Instant readAt) {

    public static MessageResponse of(Message m) {
        return new MessageResponse(m.getId(), m.getConversationId(), m.getSenderId(),
                m.getContent(), m.getSentAt(), m.getReadAt());
    }
}

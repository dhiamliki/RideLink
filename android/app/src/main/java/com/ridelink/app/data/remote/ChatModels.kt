package com.ridelink.app.data.remote

// Mirrors the backend chat DTOs (com.ridelink.chat). Fields default/nullable so a partial payload
// (e.g. a counterpart with no display name yet, a conversation with no messages) won't crash Gson.

data class Counterpart(
    val id: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
)

data class ChatMessage(
    val id: String? = null,
    val conversationId: String? = null,
    val senderId: String? = null,
    val content: String = "",
    val sentAt: String? = null,
    val readAt: String? = null,
)

data class Conversation(
    val id: String,
    val counterpart: Counterpart? = null,
    val lastMessage: ChatMessage? = null,
    val unreadCount: Int = 0,
    val createdAt: String? = null,
)

data class PagedMessages(
    val content: List<ChatMessage> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
)

// STOMP payloads sent to /app/chat.send and /app/chat.read.
data class SendMessageBody(val conversationId: String, val content: String)

data class ReadBody(val conversationId: String)

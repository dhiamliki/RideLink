package com.ridelink.chat;

import com.ridelink.auth.JwtService;
import java.util.List;
import java.util.UUID;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

// Authenticates the STOMP CONNECT frame with the same JWT access token used for REST. The token is
// read from a "Authorization: Bearer <token>" or plain "token" native header on CONNECT; an invalid
// or missing token rejects the connection. The resolved userId is bound to the session as its
// Principal, so every later frame on that session is attributable to that user.
@Component
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public StompAuthChannelInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || !StompCommand.CONNECT.equals(accessor.getCommand())) {
            return message;
        }
        UUID userId = jwtService.parseUserId(extractToken(accessor));
        if (userId == null) {
            throw new MessagingException("Invalid or missing access token");
        }
        accessor.setUser(new UsernamePasswordAuthenticationToken(userId.toString(), null, List.of()));
        return message;
    }

    private String extractToken(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        if (header != null && !header.isBlank()) {
            return header;
        }
        return accessor.getFirstNativeHeader("token");
    }
}

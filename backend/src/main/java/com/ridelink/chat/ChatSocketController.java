package com.ridelink.chat;

import com.ridelink.chat.dto.MessageResponse;
import com.ridelink.chat.dto.ReadReceipt;
import com.ridelink.chat.dto.ReadRequest;
import com.ridelink.chat.dto.SendMessageRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

// STOMP entry points. The sender is the CONNECT-authenticated Principal (never the payload), so a
// client cannot spoof another user. Persisted messages are broadcast to the conversation topic so
// the other participant receives them live; history survives via the persisted rows.
@Controller
public class ChatSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatSocketController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void send(@Valid @Payload SendMessageRequest req, Principal principal) {
        UUID me = UUID.fromString(principal.getName());
        MessageResponse saved = chatService.sendMessage(me, req.conversationId(), req.content());
        messagingTemplate.convertAndSend("/topic/conversations/" + req.conversationId(), saved);
    }

    @MessageMapping("/chat.read")
    public void read(@Valid @Payload ReadRequest req, Principal principal) {
        UUID me = UUID.fromString(principal.getName());
        ReadReceipt receipt = chatService.markRead(me, req.conversationId());
        messagingTemplate.convertAndSend("/topic/conversations/" + req.conversationId() + "/read", receipt);
    }

    // Surface eligibility/validation failures back to just the sender instead of dropping silently.
    @MessageExceptionHandler
    @SendToUser("/queue/errors")
    public Map<String, Object> onError(Throwable ex) {
        String reason = ex instanceof ResponseStatusException rse && rse.getReason() != null
                ? rse.getReason()
                : ex.getMessage();
        return Map.of("error", reason == null ? "chat error" : reason);
    }
}

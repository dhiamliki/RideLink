package com.ridelink.chat;

import com.ridelink.chat.dto.ConversationResponse;
import com.ridelink.chat.dto.PagedMessages;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/conversations")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping
    public List<ConversationResponse> mine(@AuthenticationPrincipal UUID userId) {
        return chatService.listMine(userId);
    }

    @GetMapping("/{id}/messages")
    public PagedMessages messages(@AuthenticationPrincipal UUID userId, @PathVariable UUID id,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "30") int size) {
        return chatService.messages(userId, id, page, size);
    }

    @PostMapping("/from-booking/{bookingId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse fromBooking(@AuthenticationPrincipal UUID userId, @PathVariable UUID bookingId) {
        return chatService.fromBooking(userId, bookingId);
    }

    @PostMapping("/from-proposal/{proposalId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationResponse fromProposal(@AuthenticationPrincipal UUID userId, @PathVariable UUID proposalId) {
        return chatService.fromProposal(userId, proposalId);
    }
}

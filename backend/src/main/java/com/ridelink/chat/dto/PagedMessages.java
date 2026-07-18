package com.ridelink.chat.dto;

import java.util.List;
import org.springframework.data.domain.Page;

// Mirrors the shape the Android client already uses for paged lists (see PagedResponse).
public record PagedMessages(
        List<MessageResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages) {

    public static PagedMessages of(Page<MessageResponse> p) {
        return new PagedMessages(p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}

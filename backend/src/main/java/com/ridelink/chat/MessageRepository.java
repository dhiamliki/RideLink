package com.ridelink.chat;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    Page<Message> findByConversationIdOrderBySentAtDesc(UUID conversationId, Pageable pageable);

    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(UUID conversationId);

    // Unread = a message from the counterpart (not me) that I haven't read yet.
    long countByConversationIdAndSenderIdNotAndReadAtIsNull(UUID conversationId, UUID me);

    @Modifying
    @Query("update Message m set m.readAt = :now "
            + "where m.conversationId = :conversationId and m.senderId <> :me and m.readAt is null")
    int markRead(@Param("conversationId") UUID conversationId, @Param("me") UUID me,
                 @Param("now") Instant now);
}

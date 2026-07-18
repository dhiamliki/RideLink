package com.ridelink.chat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByPairKey(String pairKey);

    @Query("select c from Conversation c where c.participantA = :me or c.participantB = :me "
            + "order by c.createdAt desc")
    List<Conversation> findMine(@Param("me") UUID me);
}

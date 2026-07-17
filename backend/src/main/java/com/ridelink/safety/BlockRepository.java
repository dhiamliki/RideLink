package com.ridelink.safety;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BlockRepository extends JpaRepository<Block, UUID> {

    boolean existsByBlockerIdAndBlockedUserId(UUID blockerId, UUID blockedUserId);

    Optional<Block> findByBlockerIdAndBlockedUserId(UUID blockerId, UUID blockedUserId);

    List<Block> findByBlockerIdOrderByCreatedAtDesc(UUID blockerId);

    // A block protects in BOTH directions: true if either user has blocked the other.
    @Query("select (count(b) > 0) from Block b "
            + "where (b.blockerId = :u1 and b.blockedUserId = :u2) "
            + "or (b.blockerId = :u2 and b.blockedUserId = :u1)")
    boolean existsBetween(@Param("u1") UUID u1, @Param("u2") UUID u2);

    // Every user id on the other side of a block involving me (in either direction).
    @Query("select case when b.blockerId = :me then b.blockedUserId else b.blockerId end "
            + "from Block b where b.blockerId = :me or b.blockedUserId = :me")
    List<UUID> findCounterpartIds(@Param("me") UUID me);
}

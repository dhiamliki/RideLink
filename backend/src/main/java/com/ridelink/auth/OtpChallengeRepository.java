package com.ridelink.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    Optional<OtpChallenge> findFirstByPhoneNumberAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phoneNumber, Instant now);

    Optional<OtpChallenge> findFirstByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

    @Modifying
    @Query("update OtpChallenge c set c.consumed = true where c.phoneNumber = :phoneNumber and c.consumed = false")
    int consumeActiveByPhoneNumber(@Param("phoneNumber") String phoneNumber);
}

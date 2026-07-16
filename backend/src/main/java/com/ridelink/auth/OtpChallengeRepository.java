package com.ridelink.auth;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OtpChallengeRepository extends JpaRepository<OtpChallenge, UUID> {

    Optional<OtpChallenge> findFirstByPhoneNumberAndConsumedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            String phoneNumber, Instant now);
}

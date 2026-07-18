package com.ridelink.chat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

// A private thread between exactly two users, unlocked by an ACCEPTED booking or proposal. The
// pairKey (both ids sorted + joined) is unique so the same two users always share ONE conversation.
@Entity
@Table(name = "conversation")
public class Conversation {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "pair_key", nullable = false, length = 73)
    private String pairKey;

    @Column(name = "participant_a", nullable = false)
    private UUID participantA;

    @Column(name = "participant_b", nullable = false)
    private UUID participantB;

    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "proposal_id")
    private UUID proposalId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Conversation() {
    }

    private Conversation(String pairKey, UUID participantA, UUID participantB, UUID bookingId, UUID proposalId) {
        this.pairKey = pairKey;
        this.participantA = participantA;
        this.participantB = participantB;
        this.bookingId = bookingId;
        this.proposalId = proposalId;
    }

    // Stable key for a pair: the two ids sorted so (A,B) and (B,A) collide on the same conversation.
    public static String pairKey(UUID u1, UUID u2) {
        return u1.compareTo(u2) <= 0 ? u1 + ":" + u2 : u2 + ":" + u1;
    }

    public static Conversation between(UUID u1, UUID u2, UUID bookingId, UUID proposalId) {
        UUID a = u1.compareTo(u2) <= 0 ? u1 : u2;
        UUID b = u1.compareTo(u2) <= 0 ? u2 : u1;
        return new Conversation(pairKey(u1, u2), a, b, bookingId, proposalId);
    }

    public boolean hasParticipant(UUID userId) {
        return participantA.equals(userId) || participantB.equals(userId);
    }

    public UUID counterpartOf(UUID userId) {
        return participantA.equals(userId) ? participantB : participantA;
    }

    public UUID getId() {
        return id;
    }

    public String getPairKey() {
        return pairKey;
    }

    public UUID getParticipantA() {
        return participantA;
    }

    public UUID getParticipantB() {
        return participantB;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public UUID getProposalId() {
        return proposalId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

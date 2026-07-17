package com.ridelink.proposal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "request_proposal")
public class RequestProposal {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProposalStatus status = ProposalStatus.PROPOSED;

    @Column(length = 500)
    private String message;

    @Column(name = "price_per_seat", precision = 10, scale = 3)
    private BigDecimal pricePerSeat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "decided_at")
    private Instant decidedAt;

    protected RequestProposal() {
    }

    public RequestProposal(UUID requestId, UUID driverId, String message, BigDecimal pricePerSeat) {
        this.requestId = requestId;
        this.driverId = driverId;
        this.message = message;
        this.pricePerSeat = pricePerSeat;
    }

    public UUID getId() {
        return id;
    }

    public UUID getRequestId() {
        return requestId;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public ProposalStatus getStatus() {
        return status;
    }

    public void setStatus(ProposalStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(Instant decidedAt) {
        this.decidedAt = decidedAt;
    }
}

package com.ridelink.proposal.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

// Both fields optional: a driver may propose with just a note, just a price, both, or neither.
public record CreateProposalRequest(
        @Size(max = 500) String message,
        @DecimalMin("0.0") BigDecimal pricePerSeat) {
}

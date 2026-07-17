package com.ridelink.proposal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.proposal.ProposalStatus;
import com.ridelink.proposal.RequestProposal;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// contact is the ACCEPTED counterpart's info from the viewer's perspective; null (and omitted)
// while the proposal is not accepted. request may be null in batch views if the request is gone.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProposalResponse(
        UUID id,
        UUID requestId,
        UUID driverId,
        ProposalStatus status,
        String message,
        BigDecimal pricePerSeat,
        Instant createdAt,
        Instant decidedAt,
        RequestSummary request,
        ContactDto contact) {

    public static ProposalResponse of(RequestProposal p, RequestSummary request, ContactDto contact) {
        return new ProposalResponse(p.getId(), p.getRequestId(), p.getDriverId(), p.getStatus(),
                p.getMessage(), p.getPricePerSeat(), p.getCreatedAt(), p.getDecidedAt(), request, contact);
    }
}

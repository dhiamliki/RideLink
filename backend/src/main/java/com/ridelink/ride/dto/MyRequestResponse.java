package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.ride.RideRequest;
import com.ridelink.ride.RideRequestStatus;
import com.ridelink.ride.TimeWindow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

// The owner's own view of a ride request: every status (no browse filtering), plus the count of
// still-pending proposals so the client can show a badge without extra calls.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyRequestResponse(
        UUID id,
        UserSummary passenger,
        RideRequestStatus status,
        LocationDto origin,
        LocationDto destination,
        LocalDate preferredDate,
        TimeWindow preferredTimeWindow,
        int seatsNeeded,
        BigDecimal maxPricePerSeat,
        String notes,
        Instant createdAt,
        int pendingProposalCount) {

    public static MyRequestResponse of(RideRequest r, UserSummary passenger, int pendingProposalCount) {
        return new MyRequestResponse(
                r.getId(), passenger, r.getStatus(),
                LocationDto.from(r.getOrigin()), LocationDto.from(r.getDestination()),
                r.getPreferredDate(), r.getPreferredTimeWindow(),
                r.getSeatsNeeded(), r.getMaxPricePerSeat(), r.getNotes(),
                r.getCreatedAt(), pendingProposalCount);
    }
}

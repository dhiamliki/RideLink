package com.ridelink.proposal.dto;

import com.ridelink.ride.RideRequest;
import com.ridelink.ride.RideRequestStatus;
import java.time.LocalDate;
import java.util.UUID;

// Compact view of the request a proposal targets, embedded in a ProposalResponse.
public record RequestSummary(
        UUID id,
        UUID passengerId,
        String originCity,
        String destCity,
        LocalDate preferredDate,
        int seatsNeeded,
        RideRequestStatus status) {

    public static RequestSummary from(RideRequest r) {
        return new RequestSummary(r.getId(), r.getPassengerId(),
                r.getOrigin().getCityName(), r.getDestination().getCityName(),
                r.getPreferredDate(), r.getSeatsNeeded(), r.getStatus());
    }
}

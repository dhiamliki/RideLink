package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.ride.RideRequest;
import com.ridelink.ride.RideRequestStatus;
import com.ridelink.ride.TimeWindow;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestResponse(
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
        Integer matchScore) {

    public static RequestResponse of(RideRequest r, UserSummary passenger, Integer matchScore) {
        return new RequestResponse(
                r.getId(), passenger, r.getStatus(),
                LocationDto.from(r.getOrigin()), LocationDto.from(r.getDestination()),
                r.getPreferredDate(), r.getPreferredTimeWindow(),
                r.getSeatsNeeded(), r.getMaxPricePerSeat(), r.getNotes(),
                r.getCreatedAt(), matchScore);
    }
}

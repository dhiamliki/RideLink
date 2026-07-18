package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.ride.RideOffer;
import com.ridelink.ride.RideOfferStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// The owner's own view of an offer: every status/seat state (no browse filtering), plus the count of
// still-pending seat requests so the client can show a badge without extra calls.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MyOfferResponse(
        UUID id,
        UserSummary driver,
        RideOfferStatus status,
        LocationDto origin,
        LocationDto destination,
        LocalDate departureDate,
        LocalTime departureTime,
        int totalSeats,
        int availableSeats,
        BigDecimal pricePerSeat,
        String notes,
        Boolean smokingAllowed,
        Boolean petsAllowed,
        Instant createdAt,
        int pendingRequestCount) {

    public static MyOfferResponse of(RideOffer o, UserSummary driver, int pendingRequestCount) {
        return new MyOfferResponse(
                o.getId(), driver, o.getStatus(),
                LocationDto.from(o.getOrigin()), LocationDto.from(o.getDestination()),
                o.getDepartureDate(), o.getDepartureTime(),
                o.getTotalSeats(), o.getAvailableSeats(), o.getPricePerSeat(),
                o.getNotes(), o.getSmokingAllowed(), o.getPetsAllowed(),
                o.getCreatedAt(), pendingRequestCount);
    }
}

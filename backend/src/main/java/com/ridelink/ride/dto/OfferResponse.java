package com.ridelink.ride.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.ride.RideOffer;
import com.ridelink.ride.RideOfferStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

// matchScore is populated only in ranked list results; omitted from single-item responses.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OfferResponse(
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
        Integer matchScore) {

    public static OfferResponse of(RideOffer o, UserSummary driver, Integer matchScore) {
        return new OfferResponse(
                o.getId(), driver, o.getStatus(),
                LocationDto.from(o.getOrigin()), LocationDto.from(o.getDestination()),
                o.getDepartureDate(), o.getDepartureTime(),
                o.getTotalSeats(), o.getAvailableSeats(), o.getPricePerSeat(),
                o.getNotes(), o.getSmokingAllowed(), o.getPetsAllowed(),
                o.getCreatedAt(), matchScore);
    }
}

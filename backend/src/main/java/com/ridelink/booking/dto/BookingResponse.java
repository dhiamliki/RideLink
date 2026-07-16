package com.ridelink.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.ridelink.booking.Booking;
import com.ridelink.booking.BookingStatus;
import java.time.Instant;
import java.util.UUID;

// contact is the ACCEPTED counterpart's info from the viewer's perspective; null (and omitted)
// while the booking is not accepted.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record BookingResponse(
        UUID id,
        UUID offerId,
        UUID passengerId,
        int seatsBooked,
        BookingStatus status,
        Instant createdAt,
        Instant decidedAt,
        ContactDto contact) {

    public static BookingResponse of(Booking b, ContactDto contact) {
        return new BookingResponse(b.getId(), b.getOfferId(), b.getPassengerId(), b.getSeatsBooked(),
                b.getStatus(), b.getCreatedAt(), b.getDecidedAt(), contact);
    }
}

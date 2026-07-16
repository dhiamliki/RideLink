package com.ridelink.booking.dto;

import jakarta.validation.constraints.Min;

// seatsBooked defaults to 1 when omitted.
public record CreateBookingRequest(@Min(1) Integer seatsBooked) {

    public int seatsBookedOrDefault() {
        return seatsBooked == null ? 1 : seatsBooked;
    }
}

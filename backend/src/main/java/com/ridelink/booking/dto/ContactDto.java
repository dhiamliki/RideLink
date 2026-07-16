package com.ridelink.booking.dto;

// Counterpart contact, revealed only once a booking is ACCEPTED.
public record ContactDto(String displayName, String phoneNumber) {
}

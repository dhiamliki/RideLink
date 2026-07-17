package com.ridelink.proposal.dto;

// Counterpart contact (driver <-> passenger), revealed only once a proposal is ACCEPTED.
public record ContactDto(String displayName, String phoneNumber) {
}

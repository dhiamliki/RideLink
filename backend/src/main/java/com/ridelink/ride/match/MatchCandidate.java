package com.ridelink.ride.match;

import java.time.Instant;
import java.time.LocalDate;

// A ride (offer or request) reduced to the fields the ranker scores against.
public record MatchCandidate(String originCity, String destCity, LocalDate date, Instant createdAt) {
}

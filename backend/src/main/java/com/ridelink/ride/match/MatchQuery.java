package com.ridelink.ride.match;

import java.time.LocalDate;

// The user's browse/filter intent. Null fields mean "unspecified" (no preference).
public record MatchQuery(String originCity, String destCity, LocalDate date) {
}

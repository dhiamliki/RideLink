package com.ridelink.ride;

// Rough preferred time-of-day for a ride request (v1 keeps it coarse instead of an exact time).
public enum TimeWindow {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT,
    ANY
}

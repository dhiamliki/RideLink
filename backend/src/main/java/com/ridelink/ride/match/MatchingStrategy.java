package com.ridelink.ride.match;

// Ranks a ride against a query, returning a 0-100 match score. v1 ships SimpleMatchingStrategy;
// the route-overlap strategy (Phase 5) will implement this same interface without touching callers.
public interface MatchingStrategy {

    int score(MatchQuery query, MatchCandidate candidate);
}

package com.ridelink.ride.match;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

// Naive v1 ranking: exact city match dominates, then date closeness, then recency (driver rating
// is not available until Phase 3, so recency stands in for it). Score is a 0-100 "% match".
@Component
public class SimpleMatchingStrategy implements MatchingStrategy {

    private static final int CITY_WEIGHT = 50;   // up to 25 origin + 25 destination
    private static final int DATE_WEIGHT = 40;   // exact date, decaying 5/day
    private static final int RECENCY_WEIGHT = 10; // decaying 1/day since posting

    @Override
    public int score(MatchQuery query, MatchCandidate candidate) {
        int cityPoints = cityPoints(query.originCity(), candidate.originCity())
                + cityPoints(query.destCity(), candidate.destCity());

        int datePoints;
        if (query.date() == null || candidate.date() == null) {
            datePoints = DATE_WEIGHT / 2; // neutral when no date preference
        } else {
            long daysApart = Math.abs(ChronoUnit.DAYS.between(query.date(), candidate.date()));
            datePoints = (int) Math.max(0, DATE_WEIGHT - daysApart * 5);
        }

        long ageDays = Math.max(0, ChronoUnit.DAYS.between(candidate.createdAt(), Instant.now()));
        int recencyPoints = (int) Math.max(0, RECENCY_WEIGHT - ageDays);

        return Math.min(100, cityPoints + datePoints + recencyPoints);
    }

    // Half the city weight per endpoint: full credit on an exact match, or when the user did not
    // constrain that endpoint (nothing to differentiate on).
    private int cityPoints(String queryCity, String candidateCity) {
        int perEndpoint = CITY_WEIGHT / 2;
        if (queryCity == null || queryCity.isBlank()) {
            return perEndpoint;
        }
        return queryCity.equalsIgnoreCase(candidateCity) ? perEndpoint : 0;
    }
}

package com.ridelink.ride.dto;

import com.ridelink.ride.TimeWindow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

// Body for create + edit of a ride request. seatsNeeded defaults to 1 when omitted.
public record RequestForm(
        @NotNull @Valid LocationDto origin,
        @NotNull @Valid LocationDto destination,
        @NotNull LocalDate preferredDate,
        @NotNull TimeWindow preferredTimeWindow,
        @Min(1) Integer seatsNeeded,
        @DecimalMin(value = "0.0") BigDecimal maxPricePerSeat,
        @Size(max = 500) String notes) {

    public int seatsNeededOrDefault() {
        return seatsNeeded == null ? 1 : seatsNeeded;
    }
}

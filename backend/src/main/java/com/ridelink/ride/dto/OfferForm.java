package com.ridelink.ride.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

// Body for create + edit of a ride offer.
public record OfferForm(
        @NotNull @Valid LocationDto origin,
        @NotNull @Valid LocationDto destination,
        @NotNull LocalDate departureDate,
        @NotNull LocalTime departureTime,
        @NotNull @Min(1) Integer totalSeats,
        @NotNull @DecimalMin(value = "0.0") BigDecimal pricePerSeat,
        @Size(max = 500) String notes,
        Boolean smokingAllowed,
        Boolean petsAllowed) {
}

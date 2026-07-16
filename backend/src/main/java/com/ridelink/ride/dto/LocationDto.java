package com.ridelink.ride.dto;

import com.ridelink.ride.Location;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LocationDto(
        @NotBlank @Size(max = 120) String cityName,
        @NotNull Double lat,
        @NotNull Double lon) {

    public Location toLocation() {
        return new Location(cityName.trim(), lat, lon);
    }

    public static LocationDto from(Location location) {
        return new LocationDto(location.getCityName(), location.getLat(), location.getLon());
    }
}

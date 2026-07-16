package com.ridelink.ride;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RideOfferRepository extends JpaRepository<RideOffer, UUID> {

    List<RideOffer> findByStatusAndDepartureDate(RideOfferStatus status, LocalDate departureDate);

    List<RideOffer> findByStatusAndOrigin_CityNameIgnoreCaseAndDestination_CityNameIgnoreCase(
            RideOfferStatus status, String originCityName, String destCityName);

    List<RideOffer> findByDriverIdOrderByCreatedAtDesc(UUID driverId);
}

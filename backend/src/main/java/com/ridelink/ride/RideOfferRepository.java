package com.ridelink.ride;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RideOfferRepository extends JpaRepository<RideOffer, UUID>, JpaSpecificationExecutor<RideOffer> {

    List<RideOffer> findByStatusAndDepartureDate(RideOfferStatus status, LocalDate departureDate);

    List<RideOffer> findByStatusAndOrigin_CityNameIgnoreCaseAndDestination_CityNameIgnoreCase(
            RideOfferStatus status, String originCityName, String destCityName);

    List<RideOffer> findByDriverIdOrderByCreatedAtDesc(UUID driverId);

    // Atomic conditional decrement: the WHERE guard (status ACTIVE + enough seats) is evaluated
    // under a row lock, so concurrent accepts can never oversell. Returns 1 on success, 0 if the
    // seats are gone (the loser gets 0 and the caller fails with 409).
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RideOffer o set o.availableSeats = o.availableSeats - :seats "
            + "where o.id = :offerId and o.status = com.ridelink.ride.RideOfferStatus.ACTIVE "
            + "and o.availableSeats >= :seats")
    int decrementAvailableSeats(@Param("offerId") UUID offerId, @Param("seats") int seats);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update RideOffer o set o.availableSeats = o.availableSeats + :seats where o.id = :offerId")
    int restoreAvailableSeats(@Param("offerId") UUID offerId, @Param("seats") int seats);
}

package com.ridelink.booking;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);

    List<Booking> findByOfferIdOrderByCreatedAtDesc(UUID offerId);

    boolean existsByOfferIdAndPassengerIdAndStatusIn(UUID offerId, UUID passengerId,
                                                     Collection<BookingStatus> statuses);

    // Bookings in the given status linking the two users as passenger<->driver (either direction).
    // Used to auto-decline pending bookings when a block is created (see safety.SafetyService).
    @Query("select b from Booking b, RideOffer o where b.offerId = o.id and b.status = :status "
            + "and ((b.passengerId = :u1 and o.driverId = :u2) "
            + "or (b.passengerId = :u2 and o.driverId = :u1))")
    List<Booking> findByStatusBetween(@Param("u1") UUID u1, @Param("u2") UUID u2,
                                      @Param("status") BookingStatus status);
}

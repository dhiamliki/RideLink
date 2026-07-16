package com.ridelink.booking;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);

    List<Booking> findByOfferIdOrderByCreatedAtDesc(UUID offerId);

    boolean existsByOfferIdAndPassengerIdAndStatusIn(UUID offerId, UUID passengerId,
                                                     Collection<BookingStatus> statuses);
}

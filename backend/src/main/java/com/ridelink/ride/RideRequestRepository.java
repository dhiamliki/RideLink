package com.ridelink.ride;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RideRequestRepository extends JpaRepository<RideRequest, UUID>, JpaSpecificationExecutor<RideRequest> {

    List<RideRequest> findByStatusAndPreferredDate(RideRequestStatus status, LocalDate preferredDate);

    List<RideRequest> findByStatusAndOrigin_CityNameIgnoreCaseAndDestination_CityNameIgnoreCase(
            RideRequestStatus status, String originCityName, String destCityName);

    List<RideRequest> findByPassengerIdOrderByCreatedAtDesc(UUID passengerId);
}

package com.ridelink.ride;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ride_request")
public class RideRequest {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "passenger_id", nullable = false)
    private UUID passengerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideRequestStatus status = RideRequestStatus.ACTIVE;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cityName", column = @Column(name = "origin_city_name", nullable = false, length = 120)),
            @AttributeOverride(name = "lat", column = @Column(name = "origin_lat", nullable = false)),
            @AttributeOverride(name = "lon", column = @Column(name = "origin_lon", nullable = false)),
    })
    private Location origin;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "cityName", column = @Column(name = "dest_city_name", nullable = false, length = 120)),
            @AttributeOverride(name = "lat", column = @Column(name = "dest_lat", nullable = false)),
            @AttributeOverride(name = "lon", column = @Column(name = "dest_lon", nullable = false)),
    })
    private Location destination;

    @Column(name = "preferred_date", nullable = false)
    private LocalDate preferredDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_time_window", nullable = false, length = 20)
    private TimeWindow preferredTimeWindow = TimeWindow.ANY;

    @Column(name = "seats_needed", nullable = false)
    private int seatsNeeded = 1;

    @Column(name = "max_price_per_seat", precision = 10, scale = 3)
    private BigDecimal maxPricePerSeat;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RideRequest() {
    }

    public RideRequest(UUID passengerId, Location origin, Location destination, LocalDate preferredDate,
                       TimeWindow preferredTimeWindow, int seatsNeeded) {
        this.passengerId = passengerId;
        this.origin = origin;
        this.destination = destination;
        this.preferredDate = preferredDate;
        this.preferredTimeWindow = preferredTimeWindow;
        this.seatsNeeded = seatsNeeded;
    }

    public UUID getId() {
        return id;
    }

    public UUID getPassengerId() {
        return passengerId;
    }

    public RideRequestStatus getStatus() {
        return status;
    }

    public void setStatus(RideRequestStatus status) {
        this.status = status;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public LocalDate getPreferredDate() {
        return preferredDate;
    }

    public void setPreferredDate(LocalDate preferredDate) {
        this.preferredDate = preferredDate;
    }

    public TimeWindow getPreferredTimeWindow() {
        return preferredTimeWindow;
    }

    public void setPreferredTimeWindow(TimeWindow preferredTimeWindow) {
        this.preferredTimeWindow = preferredTimeWindow;
    }

    public int getSeatsNeeded() {
        return seatsNeeded;
    }

    public void setSeatsNeeded(int seatsNeeded) {
        this.seatsNeeded = seatsNeeded;
    }

    public BigDecimal getMaxPricePerSeat() {
        return maxPricePerSeat;
    }

    public void setMaxPricePerSeat(BigDecimal maxPricePerSeat) {
        this.maxPricePerSeat = maxPricePerSeat;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

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
import java.time.LocalTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ride_offer")
public class RideOffer {

    @Id
    @GeneratedValue
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "driver_id", nullable = false)
    private UUID driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideOfferStatus status = RideOfferStatus.ACTIVE;

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

    @Column(name = "departure_date", nullable = false)
    private LocalDate departureDate;

    @Column(name = "departure_time", nullable = false)
    private LocalTime departureTime;

    @Column(name = "total_seats", nullable = false)
    private int totalSeats;

    @Column(name = "available_seats", nullable = false)
    private int availableSeats;

    @Column(name = "price_per_seat", nullable = false, precision = 10, scale = 3)
    private BigDecimal pricePerSeat;

    @Column(length = 500)
    private String notes;

    @Column(name = "smoking_allowed")
    private Boolean smokingAllowed;

    @Column(name = "pets_allowed")
    private Boolean petsAllowed;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected RideOffer() {
    }

    public RideOffer(UUID driverId, Location origin, Location destination, LocalDate departureDate,
                     LocalTime departureTime, int totalSeats, BigDecimal pricePerSeat) {
        this.driverId = driverId;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.totalSeats = totalSeats;
        this.availableSeats = totalSeats;
        this.pricePerSeat = pricePerSeat;
    }

    public UUID getId() {
        return id;
    }

    public UUID getDriverId() {
        return driverId;
    }

    public RideOfferStatus getStatus() {
        return status;
    }

    public void setStatus(RideOfferStatus status) {
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

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalTime departureTime) {
        this.departureTime = departureTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public BigDecimal getPricePerSeat() {
        return pricePerSeat;
    }

    public void setPricePerSeat(BigDecimal pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getSmokingAllowed() {
        return smokingAllowed;
    }

    public void setSmokingAllowed(Boolean smokingAllowed) {
        this.smokingAllowed = smokingAllowed;
    }

    public Boolean getPetsAllowed() {
        return petsAllowed;
    }

    public void setPetsAllowed(Boolean petsAllowed) {
        this.petsAllowed = petsAllowed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

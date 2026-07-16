package com.ridelink.ride;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

// Shared origin/destination endpoint: a human city name plus coordinates. Reused (with column
// overrides) as both origin and destination in offers and requests so cross-type browse/filter
// and later route matching operate on identical geo fields.
@Embeddable
public class Location {

    @Column(name = "city_name", nullable = false, length = 120)
    private String cityName;

    @Column(name = "lat", nullable = false)
    private double lat;

    @Column(name = "lon", nullable = false)
    private double lon;

    protected Location() {
    }

    public Location(String cityName, double lat, double lon) {
        this.cityName = cityName;
        this.lat = lat;
        this.lon = lon;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }
}

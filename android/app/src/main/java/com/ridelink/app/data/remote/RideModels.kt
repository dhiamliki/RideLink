package com.ridelink.app.data.remote

// Mirrors the backend ride DTOs (nested origin/destination objects, paged lists).

data class PagedResponse<T>(
    val content: List<T> = emptyList(),
    val page: Int = 0,
    val size: Int = 0,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
)

data class LocationDto(val cityName: String, val lat: Double, val lon: Double)

data class PosterSummary(val id: String?, val displayName: String?, val photoUrl: String?)

data class OfferItem(
    val id: String,
    val driver: PosterSummary?,
    val status: String?,
    val origin: LocationDto,
    val destination: LocationDto,
    val departureDate: String,
    val departureTime: String,
    val totalSeats: Int,
    val availableSeats: Int,
    val pricePerSeat: Double,
    val notes: String?,
    val smokingAllowed: Boolean?,
    val petsAllowed: Boolean?,
    val matchScore: Int?,
)

data class RequestItem(
    val id: String,
    val passenger: PosterSummary?,
    val status: String?,
    val origin: LocationDto,
    val destination: LocationDto,
    val preferredDate: String,
    val preferredTimeWindow: String,
    val seatsNeeded: Int,
    val maxPricePerSeat: Double?,
    val notes: String?,
    val matchScore: Int?,
)

// Request bodies
data class CreateOfferBody(
    val origin: LocationDto,
    val destination: LocationDto,
    val departureDate: String,
    val departureTime: String,
    val totalSeats: Int,
    val pricePerSeat: Double,
    val notes: String?,
    val smokingAllowed: Boolean?,
    val petsAllowed: Boolean?,
)

data class CreateRequestBody(
    val origin: LocationDto,
    val destination: LocationDto,
    val preferredDate: String,
    val preferredTimeWindow: String,
    val seatsNeeded: Int,
    val maxPricePerSeat: Double?,
    val notes: String?,
)

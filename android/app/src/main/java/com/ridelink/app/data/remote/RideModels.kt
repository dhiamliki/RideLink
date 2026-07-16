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

data class PosterSummary(
    val id: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val rating: Double? = null,
)

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

// --- Booking (Task 2e) ---

// Full offer view behind a feed card. Fields default so a partial backend payload won't crash Gson.
data class OfferDetail(
    val id: String,
    val driver: PosterSummary? = null,
    val origin: LocationDto,
    val destination: LocationDto,
    val departureDate: String,
    val departureTime: String,
    val availableSeats: Int = 0,
    val totalSeats: Int = 0,
    val pricePerSeat: Double = 0.0,
    val notes: String? = null,
    val smokingAllowed: Boolean? = null,
    val petsAllowed: Boolean? = null,
)

// Revealed to both parties only once a booking is ACCEPTED.
data class ContactDto(val displayName: String? = null, val phoneNumber: String? = null)

// A short summary of the trip a booking is attached to (shape kept lenient).
data class OfferBrief(
    val id: String? = null,
    val origin: LocationDto? = null,
    val destination: LocationDto? = null,
    val departureDate: String? = null,
    val departureTime: String? = null,
    val pricePerSeat: Double? = null,
)

// My booking as a passenger (GET /api/bookings/mine).
data class BookingSummary(
    val id: String,
    val offer: OfferBrief? = null,
    val status: String = "REQUESTED",
    val seatsBooked: Int = 1,
    val counterpartContact: ContactDto? = null,
)

// An incoming request on my offer (GET /api/offers/{offerId}/bookings).
data class BookingRequest(
    val id: String,
    val passenger: PosterSummary? = null,
    val seatsBooked: Int = 1,
    val status: String = "REQUESTED",
    val counterpartContact: ContactDto? = null,
)

data class CreateBookingBody(val seatsBooked: Int)

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

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

// --- Request proposals (Task 2g) ---

// Flat summary of the request a proposal targets (backend RequestSummary: origin/dest are plain
// city strings here, not nested LocationDto). Lenient defaults so partial payloads won't crash Gson.
data class RequestBrief(
    val id: String? = null,
    val passengerId: String? = null,
    val originCity: String? = null,
    val destCity: String? = null,
    val preferredDate: String? = null,
    val seatsNeeded: Int = 1,
    val status: String? = null,
)

// A driver's proposal on a request. Same shape whether fetched via /proposals/mine (driver view) or
// /requests/{id}/proposals (owner view). NOTE: the backend exposes only driverId (no driver name/
// photo); the counterpart's name+phone arrive in `contact` only once the proposal is ACCEPTED.
data class Proposal(
    val id: String,
    val requestId: String? = null,
    val driverId: String? = null,
    val status: String = "PROPOSED",
    val message: String? = null,
    val pricePerSeat: Double? = null,
    val createdAt: String? = null,
    val decidedAt: String? = null,
    val request: RequestBrief? = null,
    val contact: ContactDto? = null,
)

data class CreateProposalBody(val message: String?, val pricePerSeat: Double?)

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

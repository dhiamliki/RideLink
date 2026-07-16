package com.ridelink.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class HealthResponse(val status: String)

interface ApiService {

    @GET("api/health")
    suspend fun health(): HealthResponse

    @POST("api/auth/request-otp")
    suspend fun requestOtp(@Body body: RequestOtpRequest): RequestOtpResponse

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body body: VerifyOtpRequest): TokenResponse

    @POST("api/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): TokenResponse

    @GET("api/me")
    suspend fun me(): ProfileResponse

    @PUT("api/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ProfileResponse

    @GET("api/offers")
    suspend fun offers(
        @Query("originCity") originCity: String? = null,
        @Query("destCity") destCity: String? = null,
        @Query("date") date: String? = null,
        @Query("minSeats") minSeats: Int? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("smokingAllowed") smokingAllowed: Boolean? = null,
        @Query("petsAllowed") petsAllowed: Boolean? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponse<OfferItem>

    @GET("api/requests")
    suspend fun requests(
        @Query("originCity") originCity: String? = null,
        @Query("destCity") destCity: String? = null,
        @Query("date") date: String? = null,
        @Query("minSeats") minSeats: Int? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): PagedResponse<RequestItem>

    @POST("api/offers")
    suspend fun createOffer(@Body body: CreateOfferBody): OfferItem

    @POST("api/requests")
    suspend fun createRequest(@Body body: CreateRequestBody): RequestItem

    // --- Booking (Task 2e) ---

    @GET("api/offers/{id}")
    suspend fun offerDetail(@Path("id") id: String): OfferDetail

    @POST("api/offers/{offerId}/bookings")
    suspend fun createBooking(
        @Path("offerId") offerId: String,
        @Body body: CreateBookingBody,
    ): BookingSummary

    @GET("api/bookings/mine")
    suspend fun myBookings(): List<BookingSummary>

    @GET("api/offers/{offerId}/bookings")
    suspend fun offerBookings(@Path("offerId") offerId: String): List<BookingRequest>

    @POST("api/bookings/{id}/accept")
    suspend fun acceptBooking(@Path("id") id: String): Response<Unit>

    @POST("api/bookings/{id}/decline")
    suspend fun declineBooking(@Path("id") id: String): Response<Unit>

    @POST("api/bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: String): Response<Unit>
}

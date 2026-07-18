package com.ridelink.app.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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

    // The authenticated user's own offers — ALL statuses/seat states, newest first, each with a
    // pendingRequestCount. Not the browse feed (no filtering).
    @GET("api/offers/mine")
    suspend fun myOffers(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
    ): PagedResponse<OfferItem>

    // The authenticated user's own ride requests — ALL statuses, newest first, each with a
    // pendingProposalCount.
    @GET("api/requests/mine")
    suspend fun myRequests(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 100,
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

    // --- Request proposals (Task 2g) ---

    @GET("api/requests/{id}")
    suspend fun requestDetail(@Path("id") id: String): RequestItem

    @POST("api/requests/{requestId}/proposals")
    suspend fun createProposal(
        @Path("requestId") requestId: String,
        @Body body: CreateProposalBody,
    ): Proposal

    @GET("api/proposals/mine")
    suspend fun myProposals(): List<Proposal>

    @GET("api/requests/{requestId}/proposals")
    suspend fun requestProposals(@Path("requestId") requestId: String): List<Proposal>

    @POST("api/proposals/{id}/accept")
    suspend fun acceptProposal(@Path("id") id: String): Response<Unit>

    @POST("api/proposals/{id}/decline")
    suspend fun declineProposal(@Path("id") id: String): Response<Unit>

    @POST("api/proposals/{id}/withdraw")
    suspend fun withdrawProposal(@Path("id") id: String): Response<Unit>

    // --- Safety: report + block (Task 3b) ---

    @POST("api/reports")
    suspend fun reportUser(@Body body: CreateReportBody): Response<Unit>

    @POST("api/blocks")
    suspend fun blockUser(@Body body: CreateBlockBody): Response<Unit>

    @DELETE("api/blocks/{blockedUserId}")
    suspend fun unblockUser(@Path("blockedUserId") blockedUserId: String): Response<Unit>

    @GET("api/blocks")
    suspend fun blockedUsers(): List<BlockedUser>

    // --- Chat (Task 4b) ---

    @GET("api/conversations")
    suspend fun conversations(): List<Conversation>

    @GET("api/conversations/{id}/messages")
    suspend fun conversationMessages(
        @Path("id") id: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): PagedMessages

    @POST("api/conversations/from-booking/{bookingId}")
    suspend fun conversationFromBooking(@Path("bookingId") bookingId: String): Conversation

    @POST("api/conversations/from-proposal/{proposalId}")
    suspend fun conversationFromProposal(@Path("proposalId") proposalId: String): Conversation
}

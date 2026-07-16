package com.ridelink.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

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
}

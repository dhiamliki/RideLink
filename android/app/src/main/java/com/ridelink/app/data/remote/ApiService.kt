package com.ridelink.app.data.remote

import retrofit2.http.GET

data class HealthResponse(val status: String)

interface ApiService {

    @GET("api/health")
    suspend fun health(): HealthResponse
}

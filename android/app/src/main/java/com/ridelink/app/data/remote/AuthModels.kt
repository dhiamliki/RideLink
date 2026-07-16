package com.ridelink.app.data.remote

data class RequestOtpRequest(val phoneNumber: String)

data class RequestOtpResponse(val message: String?, val devCode: String?)

data class VerifyOtpRequest(val phoneNumber: String, val code: String)

data class RefreshRequest(val refreshToken: String)

data class TokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val isNewUser: Boolean = false,
)

data class UpdateProfileRequest(val displayName: String, val bio: String?)

data class ProfileResponse(
    val id: String,
    val phoneNumber: String,
    val displayName: String?,
    val photoUrl: String?,
    val bio: String?,
    val isProfileComplete: Boolean,
)

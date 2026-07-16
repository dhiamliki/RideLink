package com.ridelink.app.data.remote

import com.google.gson.Gson
import com.ridelink.app.data.SessionManager
import com.ridelink.app.data.local.TokenStore
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response

// Attaches the Bearer access token to authenticated calls. On a 401 it attempts a single
// refresh (using a bare client to avoid recursing through this interceptor), updates the stored
// tokens, and retries the original request. If refresh fails, tokens are cleared and a
// logged-out event is broadcast so the UI returns to login.
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val sessionManager: SessionManager,
    private val gson: Gson,
) : Interceptor {

    private val bareClient = OkHttpClient()
    private val jsonMediaType = "application/json".toMediaType()

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        // Auth endpoints are public and must not trigger the refresh loop.
        if (original.url.encodedPath.startsWith("/api/auth/")) {
            return chain.proceed(original)
        }

        val access = tokenStore.accessTokenBlocking()
        val response = chain.proceed(withBearer(original, access))
        if (response.code != 401) {
            return response
        }

        response.close()
        synchronized(this) {
            val refreshToken = tokenStore.refreshTokenBlocking()
                ?: return failSession(chain, original)
            val refreshed = tryRefresh(original, refreshToken)
                ?: return failSession(chain, original)
            tokenStore.saveBlocking(refreshed.accessToken, refreshed.refreshToken)
            return chain.proceed(withBearer(original, refreshed.accessToken))
        }
    }

    private fun withBearer(request: Request, token: String?): Request =
        if (token == null) request
        else request.newBuilder().header("Authorization", "Bearer $token").build()

    private fun tryRefresh(reference: Request, refreshToken: String): TokenResponse? {
        val baseUrl = reference.url.newBuilder().encodedPath("/api/auth/refresh").query(null).build()
        val body = gson.toJson(RefreshRequest(refreshToken)).toRequestBody(jsonMediaType)
        val request = Request.Builder().url(baseUrl).post(body).build()
        return try {
            bareClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                gson.fromJson(resp.body.string(), TokenResponse::class.java)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun failSession(chain: Interceptor.Chain, original: Request): Response {
        tokenStore.clearBlocking()
        sessionManager.notifyLoggedOut()
        // Re-issue the original (now unauthenticated) call so the caller still gets a 401 body.
        return chain.proceed(original)
    }
}

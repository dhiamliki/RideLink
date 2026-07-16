package com.ridelink.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Tokens are persisted with Jetpack DataStore (Preferences). DataStore is the current
// recommended local key/value store; it is transactional and async by default. For the raw
// JWTs this is adequate for v1 — hardware-backed encryption can be layered in later.
private val Context.authDataStore by preferencesDataStore(name = "auth")

@Singleton
class TokenStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val accessKey = stringPreferencesKey("access_token")
    private val refreshKey = stringPreferencesKey("refresh_token")

    suspend fun save(accessToken: String, refreshToken: String) {
        context.authDataStore.edit { prefs ->
            prefs[accessKey] = accessToken
            prefs[refreshKey] = refreshToken
        }
    }

    suspend fun clear() {
        context.authDataStore.edit { it.clear() }
    }

    suspend fun accessToken(): String? =
        context.authDataStore.data.map { it[accessKey] }.first()

    suspend fun refreshToken(): String? =
        context.authDataStore.data.map { it[refreshKey] }.first()

    // Blocking reads for use inside the (synchronous) OkHttp interceptor.
    fun accessTokenBlocking(): String? = runBlocking { accessToken() }

    fun refreshTokenBlocking(): String? = runBlocking { refreshToken() }

    fun saveBlocking(accessToken: String, refreshToken: String) = runBlocking { save(accessToken, refreshToken) }

    fun clearBlocking() = runBlocking { clear() }
}

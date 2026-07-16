package com.ridelink.app.ui.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.local.TokenStore
import com.ridelink.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class StartDestination { PHONE, PROFILE_SETUP, HOME }

@HiltViewModel
class StartupViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _destination = MutableStateFlow<StartDestination?>(null)
    val destination: StateFlow<StartDestination?> = _destination.asStateFlow()

    init {
        resolve()
    }

    // A stored token alone isn't trusted: GET /api/me confirms the session (the interceptor
    // silently refreshes if the access token is stale). Any failure routes to phone login.
    private fun resolve() {
        viewModelScope.launch {
            if (tokenStore.accessToken() == null) {
                _destination.value = StartDestination.PHONE
                return@launch
            }
            _destination.value = try {
                if (api.me().isProfileComplete) StartDestination.HOME else StartDestination.PROFILE_SETUP
            } catch (e: Exception) {
                StartDestination.PHONE
            }
        }
    }
}

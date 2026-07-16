package com.ridelink.app.ui.otp

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.local.TokenStore
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.VerifyOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OtpUiState(
    val code: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

enum class PostLogin { HOME, PROFILE_SETUP }

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val phoneNumber: String = savedStateHandle.get<String>("phone").orEmpty()
    private val devCode: String? = savedStateHandle.get<String>("devCode")

    private val _state = MutableStateFlow(OtpUiState(code = devCode.orEmpty()))
    val state: StateFlow<OtpUiState> = _state.asStateFlow()

    private val _verified = MutableSharedFlow<PostLogin>(extraBufferCapacity = 1)
    val verified: SharedFlow<PostLogin> = _verified

    fun onCodeChange(value: String) {
        if (value.length <= 6 && value.all { it.isDigit() }) {
            _state.value = _state.value.copy(code = value, error = null)
        }
    }

    fun verify() {
        val code = _state.value.code
        if (code.length != 6) {
            _state.value = _state.value.copy(error = "Enter the 6-digit code")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val tokens = api.verifyOtp(VerifyOtpRequest(phoneNumber, code))
                tokenStore.save(tokens.accessToken, tokens.refreshToken)
                val destination = when {
                    tokens.isNewUser -> PostLogin.PROFILE_SETUP
                    else -> if (runCatching { api.me().isProfileComplete }.getOrDefault(true)) {
                        PostLogin.HOME
                    } else {
                        PostLogin.PROFILE_SETUP
                    }
                }
                _state.value = _state.value.copy(loading = false)
                _verified.tryEmit(destination)
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Invalid or expired code")
            }
        }
    }
}

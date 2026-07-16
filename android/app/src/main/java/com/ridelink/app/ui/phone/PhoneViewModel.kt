package com.ridelink.app.ui.phone

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.RequestOtpRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PhoneUiState(
    val phoneNumber: String = "+216",
    val loading: Boolean = false,
    val error: String? = null,
)

// Carries the phone (and dev-only prefill code) to the OTP screen.
data class OtpTarget(val phoneNumber: String, val devCode: String?)

@HiltViewModel
class PhoneViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(PhoneUiState())
    val state: StateFlow<PhoneUiState> = _state.asStateFlow()

    private val _codeSent = MutableSharedFlow<OtpTarget>(extraBufferCapacity = 1)
    val codeSent: SharedFlow<OtpTarget> = _codeSent

    fun onPhoneChange(value: String) {
        _state.value = _state.value.copy(phoneNumber = value, error = null)
    }

    fun sendCode() {
        val phone = _state.value.phoneNumber.trim()
        if (!phone.matches(Regex("^\\+[1-9]\\d{6,14}$"))) {
            _state.value = _state.value.copy(error = "Enter a valid phone number, e.g. +21612345678")
            return
        }
        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                val response = api.requestOtp(RequestOtpRequest(phone))
                _state.value = _state.value.copy(loading = false)
                _codeSent.tryEmit(OtpTarget(phone, response.devCode))
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = "Could not send code. Is the backend running?")
            }
        }
    }
}

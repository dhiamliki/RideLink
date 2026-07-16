package com.ridelink.app.ui.bookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.BookingRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OfferRequestsUiState {
    data object Loading : OfferRequestsUiState
    data class Success(val requests: List<BookingRequest>) : OfferRequestsUiState
    data class Error(val message: String) : OfferRequestsUiState
}

@HiltViewModel
class OfferRequestsViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val offerId: String = checkNotNull(savedStateHandle["offerId"])

    private val _uiState = MutableStateFlow<OfferRequestsUiState>(OfferRequestsUiState.Loading)
    val uiState: StateFlow<OfferRequestsUiState> = _uiState.asStateFlow()

    // The booking id currently being accepted/declined, to disable its buttons.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = OfferRequestsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                OfferRequestsUiState.Success(api.offerBookings(offerId))
            } catch (e: Exception) {
                OfferRequestsUiState.Error("Could not load requests for this ride.")
            }
        }
    }

    fun accept(bookingId: String) = act(bookingId) { api.acceptBooking(bookingId) }

    fun decline(bookingId: String) = act(bookingId) { api.declineBooking(bookingId) }

    private fun act(bookingId: String, call: suspend () -> Unit) {
        _working.value = bookingId
        viewModelScope.launch {
            try {
                call()
            } catch (_: Exception) {
                // Reloading reflects the true server state (e.g. seats no longer available -> 409).
            } finally {
                _working.value = null
                load()
            }
        }
    }
}

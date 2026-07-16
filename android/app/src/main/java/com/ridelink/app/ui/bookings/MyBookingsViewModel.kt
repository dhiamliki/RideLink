package com.ridelink.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.BookingSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MyBookingsUiState {
    data object Loading : MyBookingsUiState
    data class Success(val bookings: List<BookingSummary>) : MyBookingsUiState
    data class Error(val message: String) : MyBookingsUiState
}

@HiltViewModel
class MyBookingsViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<MyBookingsUiState>(MyBookingsUiState.Loading)
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    // The id of the booking currently being cancelled, so its button shows progress / disables.
    private val _cancelling = MutableStateFlow<String?>(null)
    val cancelling: StateFlow<String?> = _cancelling.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = MyBookingsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                MyBookingsUiState.Success(api.myBookings())
            } catch (e: Exception) {
                MyBookingsUiState.Error("Could not load your bookings.")
            }
        }
    }

    fun cancel(bookingId: String) {
        _cancelling.value = bookingId
        viewModelScope.launch {
            try {
                api.cancelBooking(bookingId)
            } catch (_: Exception) {
                // Surfaced by reloading; the list reflects the true server state.
            } finally {
                _cancelling.value = null
                load()
            }
        }
    }
}

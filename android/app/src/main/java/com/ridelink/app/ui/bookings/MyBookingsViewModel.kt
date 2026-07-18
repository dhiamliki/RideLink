package com.ridelink.app.ui.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.BookingSummary
import com.ridelink.app.ui.chat.ChatTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // The id of the booking whose chat is being opened (get-or-create in flight).
    private val _opening = MutableStateFlow<String?>(null)
    val opening: StateFlow<String?> = _opening.asStateFlow()

    // One-shot: emitted once the conversation exists, so the screen can navigate to the chat.
    private val _openChat = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val openChat: SharedFlow<ChatTarget> = _openChat.asSharedFlow()

    init {
        load()
    }

    fun message(booking: BookingSummary) {
        _opening.value = booking.id
        viewModelScope.launch {
            try {
                val conversation = api.conversationFromBooking(booking.id)
                _openChat.emit(ChatTarget(conversation.id, booking.counterpartContact?.displayName ?: "Driver"))
            } catch (_: Exception) {
                // Stay on the list; the button re-enables so the user can retry.
            } finally {
                _opening.value = null
            }
        }
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

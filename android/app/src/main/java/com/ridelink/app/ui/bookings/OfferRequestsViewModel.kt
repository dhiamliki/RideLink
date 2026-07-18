package com.ridelink.app.ui.bookings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.BookingRequest
import com.ridelink.app.data.remote.CreateBlockBody
import com.ridelink.app.data.remote.CreateReportBody
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

sealed interface OfferRequestsUiState {
    data object Loading : OfferRequestsUiState
    data class Success(val requests: List<BookingRequest>) : OfferRequestsUiState
    data class Error(val message: String) : OfferRequestsUiState
}

@HiltViewModel
class OfferRequestsViewModel @Inject constructor(
    private val api: ApiService,
    private val refreshBus: RefreshBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val offerId: String = checkNotNull(savedStateHandle["offerId"])

    private val _uiState = MutableStateFlow<OfferRequestsUiState>(OfferRequestsUiState.Loading)
    val uiState: StateFlow<OfferRequestsUiState> = _uiState.asStateFlow()

    // Drives the pull-to-refresh spinner while a manual refresh is in flight.
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // The booking id currently being accepted/declined, to disable its buttons.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    // The booking id whose chat is being opened (get-or-create in flight).
    private val _opening = MutableStateFlow<String?>(null)
    val opening: StateFlow<String?> = _opening.asStateFlow()

    // One-shot: open a chat (from the Message button on an accepted row).
    private val _openChat = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val openChat: SharedFlow<ChatTarget> = _openChat.asSharedFlow()

    // One-shot: an accept just succeeded — surface the "Accepted — Message" snackbar.
    private val _accepted = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val accepted: SharedFlow<ChatTarget> = _accepted.asSharedFlow()

    fun load() {
        _uiState.value = OfferRequestsUiState.Loading
        fetch()
    }

    fun refresh() {
        _refreshing.value = true
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            _uiState.value = try {
                OfferRequestsUiState.Success(api.offerBookings(offerId))
            } catch (e: Exception) {
                OfferRequestsUiState.Error("Could not load requests for this ride.")
            }
            _refreshing.value = false
        }
    }

    fun accept(request: BookingRequest) {
        _working.value = request.id
        viewModelScope.launch {
            var target: ChatTarget? = null
            try {
                api.acceptBooking(request.id)
                refreshBus.refreshBrowse()
                // Surface the conversation so the driver can message the passenger right away.
                val convo = runCatching { api.conversationFromBooking(request.id) }.getOrNull()
                if (convo != null) target = ChatTarget(convo.id, passengerName(request))
            } catch (_: Exception) {
                // Reloading reflects the true server state (e.g. seats no longer available -> 409).
            } finally {
                _working.value = null
                load()
            }
            target?.let { _accepted.emit(it) }
        }
    }

    fun decline(request: BookingRequest) {
        _working.value = request.id
        viewModelScope.launch {
            try {
                api.declineBooking(request.id)
                refreshBus.refreshBrowse()
            } catch (_: Exception) {
            } finally {
                _working.value = null
                load()
            }
        }
    }

    fun message(request: BookingRequest) {
        _opening.value = request.id
        viewModelScope.launch {
            try {
                val convo = api.conversationFromBooking(request.id)
                _openChat.emit(ChatTarget(convo.id, passengerName(request)))
            } catch (_: Exception) {
                // Stay on the list; the button re-enables so the user can retry.
            } finally {
                _opening.value = null
            }
        }
    }

    private fun passengerName(request: BookingRequest): String =
        request.passenger?.displayName ?: request.counterpartContact?.displayName ?: "Passenger"

    fun report(userId: String, reason: String, detail: String?) {
        viewModelScope.launch { runCatching { api.reportUser(CreateReportBody(userId, reason, detail)) } }
    }

    // Block, then reload so the (now auto-declined / hidden) counterpart's rows update.
    fun block(userId: String) {
        viewModelScope.launch {
            runCatching { api.blockUser(CreateBlockBody(userId)) }
            refreshBus.refreshBrowse()
            load()
        }
    }
}

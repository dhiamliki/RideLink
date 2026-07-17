package com.ridelink.app.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateBlockBody
import com.ridelink.app.data.remote.CreateBookingBody
import com.ridelink.app.data.remote.CreateReportBody
import com.ridelink.app.data.remote.OfferDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface DetailUiState {
    data object Loading : DetailUiState
    data class Success(
        val offer: OfferDetail,
        val isOwner: Boolean,
        val seats: Int,
        val booking: BookingState,
    ) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

sealed interface BookingState {
    data object Idle : BookingState
    data object Submitting : BookingState
    data object Requested : BookingState
    // A terminal, non-retryable reason the seat can't be requested (own ride, already booked, full).
    data class Blocked(val message: String) : BookingState
    data class Failed(val message: String) : BookingState
}

@HiltViewModel
class RideDetailViewModel @Inject constructor(
    private val api: ApiService,
    private val refreshBus: RefreshBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val offerId: String = checkNotNull(savedStateHandle["offerId"])

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Emitted once a block finishes so the screen can navigate away (the driver's ride is now hidden).
    private val _blocked = MutableSharedFlow<Unit>()
    val blocked: SharedFlow<Unit> = _blocked.asSharedFlow()

    init {
        load()
    }

    fun report(userId: String, reason: String, detail: String?) {
        viewModelScope.launch {
            runCatching { api.reportUser(CreateReportBody(userId, reason, detail)) }
        }
    }

    fun block(userId: String) {
        viewModelScope.launch {
            runCatching { api.blockUser(CreateBlockBody(userId)) }
            refreshBus.refreshBrowse()
            _blocked.emit(Unit)
        }
    }

    fun load() {
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch {
            try {
                val offer = api.offerDetail(offerId)
                val myId = runCatching { api.me().id }.getOrNull()
                val isOwner = myId != null && offer.driver?.id == myId
                _uiState.value = DetailUiState.Success(
                    offer = offer,
                    isOwner = isOwner,
                    seats = 1,
                    booking = BookingState.Idle,
                )
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error("Could not load this ride.")
            }
        }
    }

    fun setSeats(seats: Int) {
        val s = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = s.copy(seats = seats)
    }

    fun requestSeat() {
        val s = _uiState.value as? DetailUiState.Success ?: return
        _uiState.value = s.copy(booking = BookingState.Submitting)
        viewModelScope.launch {
            _uiState.value = try {
                api.createBooking(offerId, CreateBookingBody(s.seats))
                s.copy(booking = BookingState.Requested)
            } catch (e: HttpException) {
                s.copy(booking = bookingErrorFor(e.code()))
            } catch (e: Exception) {
                s.copy(booking = BookingState.Failed("Could not request a seat. Try again."))
            }
        }
    }

    private fun bookingErrorFor(code: Int): BookingState = when (code) {
        403 -> BookingState.Blocked("This is your own ride.")
        409 -> BookingState.Blocked("This ride is full or you've already requested a seat.")
        else -> BookingState.Failed("Could not request a seat. Try again.")
    }
}

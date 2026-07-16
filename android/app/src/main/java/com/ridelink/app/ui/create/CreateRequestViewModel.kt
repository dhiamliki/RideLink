package com.ridelink.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.TunisianCity
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateRequestBody
import com.ridelink.app.data.remote.LocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

val TIME_WINDOWS = listOf("ANY", "MORNING", "AFTERNOON", "EVENING", "NIGHT")

data class CreateRequestState(
    val origin: TunisianCity? = null,
    val destination: TunisianCity? = null,
    val date: String? = null,
    val timeWindow: String = "ANY",
    val seats: Int = 1,
    val maxPrice: String = "",
    val notes: String = "",
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CreateRequestViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(CreateRequestState())
    val state: StateFlow<CreateRequestState> = _state.asStateFlow()

    private val _created = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val created: SharedFlow<Unit> = _created

    fun update(transform: (CreateRequestState) -> CreateRequestState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun submit() {
        val s = _state.value
        if (s.origin == null || s.destination == null || s.date == null) {
            _state.value = s.copy(error = "Please fill origin, destination and date")
            return
        }
        if (s.origin.name == s.destination.name) {
            _state.value = s.copy(error = "Origin and destination must differ")
            return
        }
        _state.value = s.copy(submitting = true, error = null)
        viewModelScope.launch {
            try {
                api.createRequest(
                    CreateRequestBody(
                        origin = LocationDto(s.origin.name, s.origin.lat, s.origin.lon),
                        destination = LocationDto(s.destination.name, s.destination.lat, s.destination.lon),
                        preferredDate = s.date,
                        preferredTimeWindow = s.timeWindow,
                        seatsNeeded = s.seats,
                        maxPricePerSeat = s.maxPrice.toDoubleOrNull(),
                        notes = s.notes.trim().ifEmpty { null },
                    ),
                )
                _state.value = _state.value.copy(submitting = false)
                _created.tryEmit(Unit)
            } catch (e: Exception) {
                _state.value = _state.value.copy(submitting = false, error = "Could not post request")
            }
        }
    }
}

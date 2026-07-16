package com.ridelink.app.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.TunisianCity
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateOfferBody
import com.ridelink.app.data.remote.LocationDto
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateOfferState(
    val origin: TunisianCity? = null,
    val destination: TunisianCity? = null,
    val date: String? = null,
    val time: String? = null,
    val seats: Int = 1,
    val price: String = "",
    val notes: String = "",
    val smokingAllowed: Boolean = false,
    val petsAllowed: Boolean = false,
    val submitting: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class CreateOfferViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _state = MutableStateFlow(CreateOfferState())
    val state: StateFlow<CreateOfferState> = _state.asStateFlow()

    private val _created = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val created: SharedFlow<Unit> = _created

    fun update(transform: (CreateOfferState) -> CreateOfferState) {
        _state.value = transform(_state.value).copy(error = null)
    }

    fun submit() {
        val s = _state.value
        val price = s.price.toDoubleOrNull()
        if (s.origin == null || s.destination == null || s.date == null || s.time == null || price == null) {
            _state.value = s.copy(error = "Please fill origin, destination, date, time and price")
            return
        }
        if (s.origin.name == s.destination.name) {
            _state.value = s.copy(error = "Origin and destination must differ")
            return
        }
        _state.value = s.copy(submitting = true, error = null)
        viewModelScope.launch {
            try {
                api.createOffer(
                    CreateOfferBody(
                        origin = LocationDto(s.origin.name, s.origin.lat, s.origin.lon),
                        destination = LocationDto(s.destination.name, s.destination.lat, s.destination.lon),
                        departureDate = s.date,
                        departureTime = s.time,
                        totalSeats = s.seats,
                        pricePerSeat = price,
                        notes = s.notes.trim().ifEmpty { null },
                        smokingAllowed = s.smokingAllowed,
                        petsAllowed = s.petsAllowed,
                    ),
                )
                _state.value = _state.value.copy(submitting = false)
                _created.tryEmit(Unit)
            } catch (e: Exception) {
                _state.value = _state.value.copy(submitting = false, error = "Could not post offer")
            }
        }
    }
}

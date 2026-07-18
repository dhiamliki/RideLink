package com.ridelink.app.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.OfferItem
import com.ridelink.app.data.remote.RequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// A ride I posted, plus how many seat requests are still awaiting my decision.
data class PostedRide(val offer: OfferItem, val pendingCount: Int)

sealed interface MyRidesUiState {
    data object Loading : MyRidesUiState
    data class Success(val rides: List<PostedRide>, val requests: List<RequestItem>) : MyRidesUiState
    data class Error(val message: String) : MyRidesUiState
}

// NOTE: the backend exposes no "my offers"/"my requests" endpoint, so — per the task's "use the
// closest available, don't invent backend" — this reads the public feeds and keeps only rows whose
// poster is the current user. Pending counts come from the existing per-offer bookings endpoint.
@HiltViewModel
class MyRidesViewModel @Inject constructor(
    private val api: ApiService,
    refreshBus: RefreshBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyRidesUiState>(MyRidesUiState.Loading)
    val uiState: StateFlow<MyRidesUiState> = _uiState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    init {
        viewModelScope.launch { refreshBus.browse.collect { fetch() } }
    }

    fun load() {
        _uiState.value = MyRidesUiState.Loading
        fetch()
    }

    fun refresh() {
        _refreshing.value = true
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            try {
                val myId = api.me().id
                val offers = api.offers(size = 100).content.filter { it.driver?.id != null && it.driver.id == myId }
                val requests = api.requests(size = 100).content.filter { it.passenger?.id != null && it.passenger.id == myId }
                val rides = offers.map { offer ->
                    val pending = runCatching {
                        api.offerBookings(offer.id).count { it.status.uppercase() == "REQUESTED" }
                    }.getOrDefault(0)
                    PostedRide(offer, pending)
                }
                _uiState.value = MyRidesUiState.Success(rides, requests)
            } catch (e: Exception) {
                _uiState.value = MyRidesUiState.Error("Could not load your posted rides.")
            } finally {
                _refreshing.value = false
            }
        }
    }
}

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

// Sourced from GET /api/offers/mine and /api/requests/mine: the user's OWN listings across every
// status (incl. full/cancelled/completed), with pending counts embedded — no per-row extra calls.
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
                val offers = api.myOffers().content
                val requests = api.myRequests().content
                val rides = offers.map { PostedRide(it, it.pendingRequestCount) }
                _uiState.value = MyRidesUiState.Success(rides, requests)
            } catch (e: Exception) {
                _uiState.value = MyRidesUiState.Error("Could not load your posted rides.")
            } finally {
                _refreshing.value = false
            }
        }
    }
}

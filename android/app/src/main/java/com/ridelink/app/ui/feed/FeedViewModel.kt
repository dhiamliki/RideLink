package com.ridelink.app.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.TunisianCity
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.OfferItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FeedFilters(
    val origin: TunisianCity? = null,
    val destination: TunisianCity? = null,
    val date: String? = null,
    val maxPrice: Double? = null,
    val minSeats: Int? = null,
)

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val offers: List<OfferItem>) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

@HiltViewModel
class FeedViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    private val _filters = MutableStateFlow(FeedFilters())
    val filters: StateFlow<FeedFilters> = _filters.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    fun setRoute(origin: TunisianCity?, destination: TunisianCity?, date: String?) {
        _filters.value = _filters.value.copy(origin = origin, destination = destination, date = date)
    }

    fun applyFilters(maxPrice: Double?, minSeats: Int?) {
        _filters.value = _filters.value.copy(maxPrice = maxPrice, minSeats = minSeats)
        load()
    }

    fun clear() {
        _filters.value = FeedFilters()
        load()
    }

    fun load() {
        _uiState.value = FeedUiState.Loading
        fetch()
    }

    fun refresh() {
        _refreshing.value = true
        fetch()
    }

    private fun fetch() {
        val f = _filters.value
        viewModelScope.launch {
            try {
                val result = api.offers(
                    originCity = f.origin?.name,
                    destCity = f.destination?.name,
                    date = f.date,
                    minSeats = f.minSeats,
                    maxPrice = f.maxPrice,
                )
                _uiState.value = FeedUiState.Success(result.content)
            } catch (e: Exception) {
                _uiState.value = FeedUiState.Error("Could not load rides. Is the backend running?")
            } finally {
                _refreshing.value = false
            }
        }
    }
}

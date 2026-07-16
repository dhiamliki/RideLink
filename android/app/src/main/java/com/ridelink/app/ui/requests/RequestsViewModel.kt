package com.ridelink.app.ui.requests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.TunisianCity
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.RequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RequestFilters(
    val origin: TunisianCity? = null,
    val destination: TunisianCity? = null,
    val date: String? = null,
)

sealed interface RequestsUiState {
    data object Loading : RequestsUiState
    data class Success(val requests: List<RequestItem>) : RequestsUiState
    data class Error(val message: String) : RequestsUiState
}

@HiltViewModel
class RequestsViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<RequestsUiState>(RequestsUiState.Loading)
    val uiState: StateFlow<RequestsUiState> = _uiState.asStateFlow()

    private val _filters = MutableStateFlow(RequestFilters())
    val filters: StateFlow<RequestFilters> = _filters.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    fun setRoute(origin: TunisianCity?, destination: TunisianCity?, date: String?) {
        _filters.value = _filters.value.copy(origin = origin, destination = destination, date = date)
    }

    fun load() {
        _uiState.value = RequestsUiState.Loading
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
                val result = api.requests(
                    originCity = f.origin?.name,
                    destCity = f.destination?.name,
                    date = f.date,
                )
                _uiState.value = RequestsUiState.Success(result.content)
            } catch (e: Exception) {
                _uiState.value = RequestsUiState.Error("Could not load requests. Is the backend running?")
            } finally {
                _refreshing.value = false
            }
        }
    }
}

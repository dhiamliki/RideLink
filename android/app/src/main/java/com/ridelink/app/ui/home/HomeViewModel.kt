package com.ridelink.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HealthUiState {
    data object Loading : HealthUiState
    data class Success(val status: String) : HealthUiState
    data class Error(val message: String) : HealthUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: ApiService,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HealthUiState>(HealthUiState.Loading)
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        checkHealth()
    }

    fun checkHealth() {
        _uiState.value = HealthUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                HealthUiState.Success(api.health().status)
            } catch (e: Exception) {
                HealthUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

package com.ridelink.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.local.TokenStore
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.ProfileResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val profile: ProfileResponse) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _loggedOut = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loggedOut: SharedFlow<Unit> = _loggedOut

    init {
        load()
    }

    fun load() {
        _uiState.value = HomeUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                HomeUiState.Success(api.me())
            } catch (e: Exception) {
                HomeUiState.Error("Could not load your profile")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenStore.clear()
            _loggedOut.tryEmit(Unit)
        }
    }
}

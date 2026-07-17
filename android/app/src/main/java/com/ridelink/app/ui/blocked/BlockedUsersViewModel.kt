package com.ridelink.app.ui.blocked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.BlockedUser
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface BlockedUsersUiState {
    data object Loading : BlockedUsersUiState
    data class Success(val users: List<BlockedUser>) : BlockedUsersUiState
    data class Error(val message: String) : BlockedUsersUiState
}

@HiltViewModel
class BlockedUsersViewModel @Inject constructor(
    private val api: ApiService,
    private val refreshBus: RefreshBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BlockedUsersUiState>(BlockedUsersUiState.Loading)
    val uiState: StateFlow<BlockedUsersUiState> = _uiState.asStateFlow()

    // The id currently being unblocked, so its button disables / shows progress.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = BlockedUsersUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                BlockedUsersUiState.Success(api.blockedUsers())
            } catch (e: Exception) {
                BlockedUsersUiState.Error("Could not load your blocked users.")
            }
        }
    }

    fun unblock(userId: String) {
        _working.value = userId
        viewModelScope.launch {
            try {
                api.unblockUser(userId)
            } catch (_: Exception) {
                // Reloading reflects the true server state.
            } finally {
                _working.value = null
                refreshBus.refreshBrowse() // the unblocked user's rides can appear again
                load()
            }
        }
    }
}

package com.ridelink.app.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.Conversation
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ConversationsUiState {
    data object Loading : ConversationsUiState
    data class Success(val conversations: List<Conversation>) : ConversationsUiState
    data class Error(val message: String) : ConversationsUiState
}

@HiltViewModel
class ConversationsViewModel @Inject constructor(private val api: ApiService) : ViewModel() {

    private val _uiState = MutableStateFlow<ConversationsUiState>(ConversationsUiState.Loading)
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun load() {
        _uiState.value = ConversationsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                ConversationsUiState.Success(api.conversations())
            } catch (e: Exception) {
                ConversationsUiState.Error("Could not load your messages.")
            }
        }
    }
}

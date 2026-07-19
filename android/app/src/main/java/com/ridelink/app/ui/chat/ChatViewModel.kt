package com.ridelink.app.ui.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.chat.ChatClient
import com.ridelink.app.data.chat.ChatConnectionState
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.ChatMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// One-shot navigation target emitted by the "Message" entry points (accepted booking / proposal).
data class ChatTarget(val conversationId: String, val name: String)

sealed interface ChatHistoryState {
    data object Loading : ChatHistoryState
    data object Ready : ChatHistoryState
    data class Error(val message: String) : ChatHistoryState
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: ApiService,
    private val chatClient: ChatClient,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val conversationId: String = checkNotNull(savedStateHandle["conversationId"])

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _history = MutableStateFlow<ChatHistoryState>(ChatHistoryState.Loading)
    val history: StateFlow<ChatHistoryState> = _history.asStateFlow()

    private val _myId = MutableStateFlow<String?>(null)
    val myId: StateFlow<String?> = _myId.asStateFlow()

    val connectionState: StateFlow<ChatConnectionState> =
        chatClient.state.stateIn(viewModelScope, SharingStarted.Eagerly, ChatConnectionState.Disconnected)

    init {
        loadHistory()
        viewModelScope.launch { _myId.value = runCatching { api.me().id }.getOrNull() }
        observeIncoming()
    }

    fun loadHistory() {
        _history.value = ChatHistoryState.Loading
        viewModelScope.launch {
            try {
                // Backend returns newest-first; the list is kept newest-first and rendered reversed.
                _messages.value = api.conversationMessages(conversationId).content
                _history.value = ChatHistoryState.Ready
                markRead()
            } catch (e: Exception) {
                _history.value = ChatHistoryState.Error("Could not load this conversation.")
            }
        }
    }

    private fun observeIncoming() {
        viewModelScope.launch {
            chatClient.incoming(conversationId).collect { msg ->
                if (_messages.value.none { it.id != null && it.id == msg.id }) {
                    _messages.value = listOf(msg) + _messages.value
                }
                if (msg.senderId != _myId.value) markRead()
            }
        }
    }

    fun send(text: String) {
        val content = text.trim()
        if (content.isEmpty()) return
        viewModelScope.launch {
            // The message echoes back on the topic subscription, so it appends via observeIncoming().
            runCatching { chatClient.send(conversationId, content) }
        }
    }

    private fun markRead() {
        viewModelScope.launch { chatClient.markRead(conversationId) }
    }

    // No explicit teardown here: cancelling viewModelScope cancels the observeIncoming collector, which
    // releases ChatClient's subscriber refcount and disconnects the shared session once no chat screen
    // is left collecting. Tearing down from onCleared would race the next screen's connect/subscribe.
}

package com.ridelink.app.ui.proposals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateBlockBody
import com.ridelink.app.data.remote.CreateReportBody
import com.ridelink.app.data.remote.Proposal
import com.ridelink.app.ui.chat.ChatTarget
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MyProposalsUiState {
    data object Loading : MyProposalsUiState
    data class Success(val proposals: List<Proposal>) : MyProposalsUiState
    data class Error(val message: String) : MyProposalsUiState
}

@HiltViewModel
class MyProposalsViewModel @Inject constructor(
    private val api: ApiService,
    private val refreshBus: RefreshBus,
) : ViewModel() {

    private val _uiState = MutableStateFlow<MyProposalsUiState>(MyProposalsUiState.Loading)
    val uiState: StateFlow<MyProposalsUiState> = _uiState.asStateFlow()

    // Drives the pull-to-refresh spinner while a manual refresh is in flight.
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // The id of the proposal currently being withdrawn, so its button shows progress / disables.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    // The id of the proposal whose chat is being opened (get-or-create in flight).
    private val _opening = MutableStateFlow<String?>(null)
    val opening: StateFlow<String?> = _opening.asStateFlow()

    // One-shot: emitted once the conversation exists, so the screen can navigate to the chat.
    private val _openChat = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val openChat: SharedFlow<ChatTarget> = _openChat.asSharedFlow()

    fun message(proposal: Proposal) {
        _opening.value = proposal.id
        viewModelScope.launch {
            try {
                val conversation = api.conversationFromProposal(proposal.id)
                _openChat.emit(ChatTarget(conversation.id, proposal.contact?.displayName ?: "Passenger"))
            } catch (_: Exception) {
                // Stay on the list; the button re-enables so the user can retry.
            } finally {
                _opening.value = null
            }
        }
    }

    fun load() {
        _uiState.value = MyProposalsUiState.Loading
        fetch()
    }

    fun refresh() {
        _refreshing.value = true
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            _uiState.value = try {
                MyProposalsUiState.Success(api.myProposals())
            } catch (e: Exception) {
                MyProposalsUiState.Error("Could not load your proposals.")
            }
            _refreshing.value = false
        }
    }

    fun withdraw(proposalId: String) {
        _working.value = proposalId
        viewModelScope.launch {
            try {
                api.withdrawProposal(proposalId)
            } catch (_: Exception) {
                // Reloading reflects the true server state.
            } finally {
                _working.value = null
                // Invalidate the browse lists so the request owner's view updates on return.
                refreshBus.refreshBrowse()
                load()
            }
        }
    }

    fun report(userId: String, reason: String, detail: String?) {
        viewModelScope.launch { runCatching { api.reportUser(CreateReportBody(userId, reason, detail)) } }
    }

    // Block the request owner, then reload so the (now auto-declined) proposal updates.
    fun block(userId: String) {
        viewModelScope.launch {
            runCatching { api.blockUser(CreateBlockBody(userId)) }
            refreshBus.refreshBrowse()
            load()
        }
    }
}

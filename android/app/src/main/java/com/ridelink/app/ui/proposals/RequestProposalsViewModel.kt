package com.ridelink.app.ui.proposals

import androidx.lifecycle.SavedStateHandle
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

sealed interface RequestProposalsUiState {
    data object Loading : RequestProposalsUiState
    data class Success(val proposals: List<Proposal>) : RequestProposalsUiState
    data class Error(val message: String) : RequestProposalsUiState
}

@HiltViewModel
class RequestProposalsViewModel @Inject constructor(
    private val api: ApiService,
    private val refreshBus: RefreshBus,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow<RequestProposalsUiState>(RequestProposalsUiState.Loading)
    val uiState: StateFlow<RequestProposalsUiState> = _uiState.asStateFlow()

    // Drives the pull-to-refresh spinner while a manual refresh is in flight.
    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // The proposal id currently being accepted/declined, to disable its buttons.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    // The proposal id whose chat is being opened (get-or-create in flight).
    private val _opening = MutableStateFlow<String?>(null)
    val opening: StateFlow<String?> = _opening.asStateFlow()

    // One-shot: open a chat (from the Message button on an accepted row).
    private val _openChat = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val openChat: SharedFlow<ChatTarget> = _openChat.asSharedFlow()

    // One-shot: an accept just succeeded — surface the "Accepted — Message" snackbar.
    private val _accepted = MutableSharedFlow<ChatTarget>(extraBufferCapacity = 1)
    val accepted: SharedFlow<ChatTarget> = _accepted.asSharedFlow()

    fun load() {
        _uiState.value = RequestProposalsUiState.Loading
        fetch()
    }

    fun refresh() {
        _refreshing.value = true
        fetch()
    }

    private fun fetch() {
        viewModelScope.launch {
            _uiState.value = try {
                RequestProposalsUiState.Success(api.requestProposals(requestId))
            } catch (e: Exception) {
                RequestProposalsUiState.Error("Could not load proposals for this request.")
            }
            _refreshing.value = false
        }
    }

    fun accept(proposal: Proposal) {
        _working.value = proposal.id
        viewModelScope.launch {
            var target: ChatTarget? = null
            try {
                api.acceptProposal(proposal.id)
                refreshBus.refreshBrowse()
                // Surface the conversation so the passenger can message the driver right away.
                val convo = runCatching { api.conversationFromProposal(proposal.id) }.getOrNull()
                if (convo != null) target = ChatTarget(convo.id, proposal.contact?.displayName ?: "Driver")
            } catch (_: Exception) {
                // Reloading reflects the true server state (e.g. already decided -> 409).
            } finally {
                _working.value = null
                load()
            }
            target?.let { _accepted.emit(it) }
        }
    }

    fun decline(proposal: Proposal) {
        _working.value = proposal.id
        viewModelScope.launch {
            try {
                api.declineProposal(proposal.id)
                refreshBus.refreshBrowse()
            } catch (_: Exception) {
            } finally {
                _working.value = null
                load()
            }
        }
    }

    fun message(proposal: Proposal) {
        _opening.value = proposal.id
        viewModelScope.launch {
            try {
                val convo = api.conversationFromProposal(proposal.id)
                _openChat.emit(ChatTarget(convo.id, proposal.contact?.displayName ?: "Driver"))
            } catch (_: Exception) {
                // Stay on the list; the button re-enables so the user can retry.
            } finally {
                _opening.value = null
            }
        }
    }

    fun report(userId: String, reason: String, detail: String?) {
        viewModelScope.launch { runCatching { api.reportUser(CreateReportBody(userId, reason, detail)) } }
    }

    // Block, then reload so the (now auto-declined) proposal from that driver updates.
    fun block(userId: String) {
        viewModelScope.launch {
            runCatching { api.blockUser(CreateBlockBody(userId)) }
            refreshBus.refreshBrowse()
            load()
        }
    }
}

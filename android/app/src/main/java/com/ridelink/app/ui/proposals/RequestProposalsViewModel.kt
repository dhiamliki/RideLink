package com.ridelink.app.ui.proposals

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.RefreshBus
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateBlockBody
import com.ridelink.app.data.remote.CreateReportBody
import com.ridelink.app.data.remote.Proposal
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    // The proposal id currently being accepted/declined, to disable its buttons.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RequestProposalsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                RequestProposalsUiState.Success(api.requestProposals(requestId))
            } catch (e: Exception) {
                RequestProposalsUiState.Error("Could not load proposals for this request.")
            }
        }
    }

    fun accept(proposalId: String) = act(proposalId) { api.acceptProposal(proposalId) }

    fun decline(proposalId: String) = act(proposalId) { api.declineProposal(proposalId) }

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

    private fun act(proposalId: String, call: suspend () -> Unit) {
        _working.value = proposalId
        viewModelScope.launch {
            try {
                call()
            } catch (_: Exception) {
                // Reloading reflects the true server state (e.g. already decided -> 409).
            } finally {
                _working.value = null
                load()
            }
        }
    }
}

package com.ridelink.app.ui.proposals

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

    // The id of the proposal currently being withdrawn, so its button shows progress / disables.
    private val _working = MutableStateFlow<String?>(null)
    val working: StateFlow<String?> = _working.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = MyProposalsUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                MyProposalsUiState.Success(api.myProposals())
            } catch (e: Exception) {
                MyProposalsUiState.Error("Could not load your proposals.")
            }
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

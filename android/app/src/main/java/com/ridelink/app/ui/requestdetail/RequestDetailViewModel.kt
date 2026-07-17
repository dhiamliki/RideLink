package com.ridelink.app.ui.requestdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ridelink.app.data.remote.ApiService
import com.ridelink.app.data.remote.CreateProposalBody
import com.ridelink.app.data.remote.RequestItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

sealed interface RequestDetailUiState {
    data object Loading : RequestDetailUiState
    data class Success(
        val request: RequestItem,
        val isOwner: Boolean,
        val message: String,
        val price: String,
        val proposal: ProposalState,
    ) : RequestDetailUiState
    data class Error(val message: String) : RequestDetailUiState
}

sealed interface ProposalState {
    data object Idle : ProposalState
    data object Submitting : ProposalState
    data object Proposed : ProposalState
    // Terminal, non-retryable reason a proposal can't be made (own request, already proposed).
    data class Blocked(val message: String) : ProposalState
    data class Failed(val message: String) : ProposalState
}

@HiltViewModel
class RequestDetailViewModel @Inject constructor(
    private val api: ApiService,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val requestId: String = checkNotNull(savedStateHandle["requestId"])

    private val _uiState = MutableStateFlow<RequestDetailUiState>(RequestDetailUiState.Loading)
    val uiState: StateFlow<RequestDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = RequestDetailUiState.Loading
        viewModelScope.launch {
            try {
                val request = api.requestDetail(requestId)
                val myId = runCatching { api.me().id }.getOrNull()
                val isOwner = myId != null && request.passenger?.id == myId
                _uiState.value = RequestDetailUiState.Success(
                    request = request,
                    isOwner = isOwner,
                    message = "",
                    price = "",
                    proposal = ProposalState.Idle,
                )
            } catch (e: Exception) {
                _uiState.value = RequestDetailUiState.Error("Could not load this request.")
            }
        }
    }

    fun setMessage(value: String) {
        val s = _uiState.value as? RequestDetailUiState.Success ?: return
        _uiState.value = s.copy(message = value)
    }

    fun setPrice(value: String) {
        val s = _uiState.value as? RequestDetailUiState.Success ?: return
        _uiState.value = s.copy(price = value.filter { it.isDigit() || it == '.' })
    }

    fun propose() {
        val s = _uiState.value as? RequestDetailUiState.Success ?: return
        _uiState.value = s.copy(proposal = ProposalState.Submitting)
        viewModelScope.launch {
            _uiState.value = try {
                api.createProposal(
                    requestId,
                    CreateProposalBody(
                        message = s.message.trim().ifBlank { null },
                        pricePerSeat = s.price.trim().toDoubleOrNull(),
                    ),
                )
                s.copy(proposal = ProposalState.Proposed)
            } catch (e: HttpException) {
                s.copy(proposal = proposalErrorFor(e.code()))
            } catch (e: Exception) {
                s.copy(proposal = ProposalState.Failed("Could not send your proposal. Try again."))
            }
        }
    }

    private fun proposalErrorFor(code: Int): ProposalState = when (code) {
        403 -> ProposalState.Blocked("This is your own request.")
        409 -> ProposalState.Blocked("You've already proposed on this request.")
        else -> ProposalState.Failed("Could not send your proposal. Try again.")
    }
}

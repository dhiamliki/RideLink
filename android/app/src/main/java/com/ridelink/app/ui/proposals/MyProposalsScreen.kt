package com.ridelink.app.ui.proposals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.Proposal
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.SafetyMenu
import com.ridelink.app.ui.common.SecondaryButton
import com.ridelink.app.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProposalsScreen(
    onBack: () -> Unit,
    viewModel: MyProposalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val working by viewModel.working.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My proposals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = viewModel::load,
            modifier = Modifier.fillMaxSize().padding(padding),
        ) {
            when (val state = uiState) {
                is MyProposalsUiState.Loading -> LoadingState()
                is MyProposalsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is MyProposalsUiState.Success ->
                    if (state.proposals.isEmpty()) {
                        EmptyState("You haven't proposed on any requests yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md),
                        ) {
                            items(state.proposals, key = { it.id }) {
                                ProposalCard(it, working == it.id, viewModel::withdraw,
                                    viewModel::report, viewModel::block)
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun ProposalCard(
    proposal: Proposal,
    working: Boolean,
    onWithdraw: (String) -> Unit,
    onReport: (String, String, String?) -> Unit,
    onBlock: (String) -> Unit,
) {
    val status = proposal.status.uppercase()
    val route = proposal.request?.let { "${it.originCity ?: "?"}  →  ${it.destCity ?: "?"}" }
    // Counterpart on this screen is the request owner; their name arrives via `contact` once ACCEPTED.
    val ownerId = proposal.request?.passengerId
    val ownerName = proposal.contact?.displayName ?: "Passenger"
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(route ?: "Request", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(proposal.status)
                if (ownerId != null) {
                    SafetyMenu(
                        targetName = ownerName,
                        onReport = { reason, detail -> onReport(ownerId, reason, detail) },
                        onBlock = { onBlock(ownerId) },
                    )
                }
            }
        }
        proposal.request?.preferredDate?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        proposal.message?.takeIf { it.isNotBlank() }?.let {
            Text("“$it”", style = MaterialTheme.typography.bodyMedium)
        }
        proposal.pricePerSeat?.let {
            Text("Proposed $it DT per seat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }

        if (status == "ACCEPTED") {
            ContactCard(proposal.contact?.displayName, proposal.contact?.phoneNumber)
        }

        if (status == "PROPOSED") {
            SecondaryButton(if (working) "Withdrawing…" else "Withdraw", onClick = { onWithdraw(proposal.id) }, enabled = !working)
        }
    }
}

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.Proposal
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.SafetyMenu
import com.ridelink.app.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestProposalsScreen(
    onBack: () -> Unit,
    viewModel: RequestProposalsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val working by viewModel.working.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Proposals on my request") },
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
                is RequestProposalsUiState.Loading -> LoadingState()
                is RequestProposalsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is RequestProposalsUiState.Success ->
                    if (state.proposals.isEmpty()) {
                        EmptyState("No proposals on this request yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.proposals, key = { it.id }) {
                                ProposalCard(it, working == it.id, viewModel::accept, viewModel::decline,
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
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onReport: (String, String, String?) -> Unit,
    onBlock: (String) -> Unit,
) {
    val status = proposal.status.uppercase()
    // The backend exposes only driverId on this view; the driver's name arrives via `contact`
    // once ACCEPTED. Show that name if we have it, otherwise a neutral label.
    val driverName = proposal.contact?.displayName ?: "Driver"
    val driverId = proposal.driverId
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Avatar(driverName)
                    Text(driverName, style = MaterialTheme.typography.titleMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusPill(proposal.status)
                    if (driverId != null) {
                        SafetyMenu(
                            targetName = driverName,
                            onReport = { reason, detail -> onReport(driverId, reason, detail) },
                            onBlock = { onBlock(driverId) },
                        )
                    }
                }
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onAccept(proposal.id) },
                        enabled = !working,
                        modifier = Modifier.weight(1f),
                    ) { Text("Accept") }
                    OutlinedButton(
                        onClick = { onDecline(proposal.id) },
                        enabled = !working,
                        modifier = Modifier.weight(1f),
                    ) { Text("Decline") }
                }
            }
        }
    }
}

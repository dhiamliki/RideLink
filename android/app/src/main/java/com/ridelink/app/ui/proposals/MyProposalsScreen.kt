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
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
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
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.proposals, key = { it.id }) {
                                ProposalCard(it, working == it.id, viewModel::withdraw)
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun ProposalCard(proposal: Proposal, working: Boolean, onWithdraw: (String) -> Unit) {
    val status = proposal.status.uppercase()
    val route = proposal.request?.let { "${it.originCity ?: "?"}  →  ${it.destCity ?: "?"}" }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(route ?: "Request", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusPill(proposal.status)
            }
            proposal.request?.preferredDate?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
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
                OutlinedButton(
                    onClick = { onWithdraw(proposal.id) },
                    enabled = !working,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (working) "Withdrawing…" else "Withdraw") }
            }
        }
    }
}

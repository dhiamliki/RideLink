package com.ridelink.app.ui.requestdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.RequestItem
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailScreen(
    onBack: () -> Unit,
    onViewProposals: (String) -> Unit,
    viewModel: RequestDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is RequestDetailUiState.Loading -> LoadingState()
                is RequestDetailUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is RequestDetailUiState.Success -> DetailContent(
                    state = state,
                    onMessage = viewModel::setMessage,
                    onPrice = viewModel::setPrice,
                    onPropose = viewModel::propose,
                    onViewProposals = { onViewProposals(state.request.id) },
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: RequestDetailUiState.Success,
    onMessage: (String) -> Unit,
    onPrice: (String) -> Unit,
    onPropose: () -> Unit,
    onViewProposals: () -> Unit,
) {
    val request = state.request
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(request.passenger?.displayName)
            Column {
                Text(request.passenger?.displayName ?: "Passenger", style = MaterialTheme.typography.titleMedium)
                request.passenger?.rating?.let {
                    Text("★ ${"%.1f".format(it)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Text(
            "${request.origin.cityName}  →  ${request.destination.cityName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Preferred", "${request.preferredDate} · ${request.preferredTimeWindow}")
                InfoRow("Seats needed", "${request.seatsNeeded}")
                InfoRow("Max price per seat", request.maxPricePerSeat?.let { "$it DT" } ?: "No budget set")
            }
        }

        request.notes?.takeIf { it.isNotBlank() }?.let {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (state.isOwner) {
            Banner(
                "This is your request. Drivers can propose to take you.",
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onViewProposals, modifier = Modifier.fillMaxWidth()) {
                Text("View proposals on this request")
            }
        } else {
            ProposalSection(state, onMessage, onPrice, onPropose)
        }
    }
}

@Composable
private fun ProposalSection(
    state: RequestDetailUiState.Success,
    onMessage: (String) -> Unit,
    onPrice: (String) -> Unit,
    onPropose: () -> Unit,
) {
    when (val p = state.proposal) {
        is ProposalState.Proposed -> Banner(
            "Proposal sent — the passenger will accept or decline. Track it under My proposals.",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )

        is ProposalState.Blocked -> Banner(
            p.message,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("I can take you", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = state.message,
                onValueChange = onMessage,
                label = { Text("Message (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.price,
                onValueChange = onPrice,
                label = { Text("Proposed price per seat (optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            if (p is ProposalState.Failed) {
                Text(p.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onPropose,
                enabled = p !is ProposalState.Submitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (p is ProposalState.Submitting) "Sending…" else "I can take you")
            }
        }
    }
}

@Composable
private fun Banner(text: String, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color) {
    Surface(color = bg, shape = MaterialTheme.shapes.medium, modifier = Modifier.fillMaxWidth()) {
        Text(text, color = fg, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

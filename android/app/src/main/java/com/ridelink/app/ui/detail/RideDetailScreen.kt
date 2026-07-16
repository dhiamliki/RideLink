package com.ridelink.app.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.OfferDetail
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.SeatStepper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideDetailScreen(
    onBack: () -> Unit,
    onViewRequests: (String) -> Unit,
    viewModel: RideDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ride details") },
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
                is DetailUiState.Loading -> LoadingState()
                is DetailUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is DetailUiState.Success -> DetailContent(
                    state = state,
                    onSeats = viewModel::setSeats,
                    onRequest = viewModel::requestSeat,
                    onViewRequests = { onViewRequests(state.offer.id) },
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: DetailUiState.Success,
    onSeats: (Int) -> Unit,
    onRequest: () -> Unit,
    onViewRequests: () -> Unit,
) {
    val offer = state.offer
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Avatar(offer.driver?.displayName)
            Column {
                Text(offer.driver?.displayName ?: "Driver", style = MaterialTheme.typography.titleMedium)
                offer.driver?.rating?.let {
                    Text("★ ${"%.1f".format(it)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
        }

        Text(
            "${offer.origin.cityName}  →  ${offer.destination.cityName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Departure", "${offer.departureDate} · ${offer.departureTime}")
                InfoRow("Seats", "${offer.availableSeats} of ${offer.totalSeats} available")
                InfoRow("Price per seat", "${offer.pricePerSeat} DT")
                InfoRow("Smoking", if (offer.smokingAllowed == true) "Allowed" else "Not allowed")
                InfoRow("Pets", if (offer.petsAllowed == true) "Allowed" else "Not allowed")
            }
        }

        offer.notes?.takeIf { it.isNotBlank() }?.let {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Notes", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (state.isOwner) {
            Button(onClick = onViewRequests, modifier = Modifier.fillMaxWidth()) {
                Text("View requests on this ride")
            }
        } else {
            BookingSection(offer, state, onSeats, onRequest)
        }
    }
}

@Composable
private fun BookingSection(
    offer: OfferDetail,
    state: DetailUiState.Success,
    onSeats: (Int) -> Unit,
    onRequest: () -> Unit,
) {
    when (val b = state.booking) {
        is BookingState.Requested -> Banner(
            "Seat requested — the driver will accept or decline. Track it under My bookings.",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )

        is BookingState.Blocked -> Banner(
            b.message,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )

        else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (offer.availableSeats > 1) {
                SeatStepper("Seats", state.seats, onSeats, min = 1, max = offer.availableSeats)
            }
            if (b is BookingState.Failed) {
                Text(b.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            Button(
                onClick = onRequest,
                enabled = b !is BookingState.Submitting && offer.availableSeats > 0,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (offer.availableSeats > 0) "Request a seat" else "Ride is full")
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

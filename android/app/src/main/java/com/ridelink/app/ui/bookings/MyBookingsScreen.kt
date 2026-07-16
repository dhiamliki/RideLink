package com.ridelink.app.ui.bookings

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
import com.ridelink.app.data.remote.BookingSummary
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBookingsScreen(
    onBack: () -> Unit,
    viewModel: MyBookingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val cancelling by viewModel.cancelling.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My bookings") },
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
                is MyBookingsUiState.Loading -> LoadingState()
                is MyBookingsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is MyBookingsUiState.Success ->
                    if (state.bookings.isEmpty()) {
                        EmptyState("You haven't requested any rides yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.bookings, key = { it.id }) {
                                BookingCard(it, cancelling == it.id, viewModel::cancel)
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun BookingCard(booking: BookingSummary, cancelling: Boolean, onCancel: (String) -> Unit) {
    val route = booking.offer?.let { "${it.origin?.cityName ?: "?"}  →  ${it.destination?.cityName ?: "?"}" }
    val status = booking.status.uppercase()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(route ?: "Ride", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                StatusPill(booking.status)
            }
            booking.offer?.let { o ->
                if (o.departureDate != null) {
                    Text("${o.departureDate} · ${o.departureTime ?: ""}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }
            Text("${booking.seatsBooked} seat(s)", style = MaterialTheme.typography.bodyMedium)

            if (status == "ACCEPTED") {
                ContactCard(booking.counterpartContact?.displayName, booking.counterpartContact?.phoneNumber)
            }

            if (status == "REQUESTED" || status == "ACCEPTED") {
                OutlinedButton(
                    onClick = { onCancel(booking.id) },
                    enabled = !cancelling,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(if (cancelling) "Cancelling…" else "Cancel booking") }
            }
        }
    }
}

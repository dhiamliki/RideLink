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
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.BookingRequest
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.SafetyMenu
import com.ridelink.app.ui.common.SecondaryButton
import com.ridelink.app.ui.common.StatusPill

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRequestsScreen(
    onBack: () -> Unit,
    viewModel: OfferRequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val working by viewModel.working.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Requests on my ride") },
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
                is OfferRequestsUiState.Loading -> LoadingState()
                is OfferRequestsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is OfferRequestsUiState.Success ->
                    if (state.requests.isEmpty()) {
                        EmptyState("No requests on this ride yet")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md),
                        ) {
                            items(state.requests, key = { it.id }) {
                                RequestCard(it, working == it.id, viewModel::accept, viewModel::decline,
                                    viewModel::report, viewModel::block)
                            }
                        }
                    }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: BookingRequest,
    working: Boolean,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onReport: (String, String, String?) -> Unit,
    onBlock: (String) -> Unit,
) {
    val status = request.status.uppercase()
    val passengerName = request.passenger?.displayName ?: "Passenger"
    val passengerId = request.passenger?.id
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm), modifier = Modifier.weight(1f)) {
                Avatar(request.passenger?.displayName, size = 38)
                Text(passengerName, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(request.status)
                if (passengerId != null) {
                    SafetyMenu(
                        targetName = passengerName,
                        onReport = { reason, detail -> onReport(passengerId, reason, detail) },
                        onBlock = { onBlock(passengerId) },
                    )
                }
            }
        }
        Text("${request.seatsBooked} seat(s) requested", style = MaterialTheme.typography.bodyMedium)

        if (status == "ACCEPTED") {
            ContactCard(request.counterpartContact?.displayName, request.counterpartContact?.phoneNumber)
        }

        if (status == "REQUESTED") {
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                PrimaryButton("Accept", onClick = { onAccept(request.id) }, enabled = !working, modifier = Modifier.weight(1f))
                SecondaryButton("Decline", onClick = { onDecline(request.id) }, enabled = !working, modifier = Modifier.weight(1f))
            }
        }
    }
}

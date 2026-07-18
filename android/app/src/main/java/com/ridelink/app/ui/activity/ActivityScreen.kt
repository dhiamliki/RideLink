package com.ridelink.app.ui.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.RequestItem
import com.ridelink.app.ui.bookings.BookingCard
import com.ridelink.app.ui.bookings.MyBookingsUiState
import com.ridelink.app.ui.bookings.MyBookingsViewModel
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.SectionHeader
import com.ridelink.app.ui.common.SegmentedToggle
import com.ridelink.app.ui.common.Tag
import com.ridelink.app.ui.common.Tone
import com.ridelink.app.ui.common.formatDateTime
import com.ridelink.app.ui.proposals.MyProposalCard
import com.ridelink.app.ui.proposals.MyProposalsUiState
import com.ridelink.app.ui.proposals.MyProposalsViewModel

// The Activity tab: everything I'm involved in, split by the role I'm playing.
// Driver = things I posted (my rides + my requests); Passenger = things I joined (bookings + proposals).
@Composable
fun ActivityScreen(
    onOpenOfferRequests: (String) -> Unit,
    onOpenRequestProposals: (String) -> Unit,
    onOpenChat: (conversationId: String, counterpartName: String) -> Unit,
) {
    var driver by rememberSaveable { mutableStateOf(true) }
    Column(Modifier.fillMaxSize()) {
        SegmentedToggle(
            options = listOf("Driver", "Passenger"),
            selectedIndex = if (driver) 0 else 1,
            onSelect = { driver = it == 0 },
            modifier = Modifier.padding(horizontal = Dimens.screen, vertical = Dimens.sm),
        )
        if (driver) {
            DriverActivity(onOpenOfferRequests, onOpenRequestProposals)
        } else {
            PassengerActivity(onOpenChat)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DriverActivity(
    onOpenOfferRequests: (String) -> Unit,
    onOpenRequestProposals: (String) -> Unit,
    viewModel: MyRidesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = viewModel::refresh, modifier = Modifier.fillMaxSize()) {
        when (val s = state) {
            is MyRidesUiState.Loading -> CenteredSpinner()
            is MyRidesUiState.Error -> ErrorState(s.message, onRetry = viewModel::load)
            is MyRidesUiState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
                verticalArrangement = Arrangement.spacedBy(Dimens.md),
            ) {
                item { SectionHeader("My posted rides") }
                if (s.rides.isEmpty()) {
                    item { EmptyLine("You haven't posted any rides yet.") }
                } else {
                    items(s.rides, key = { it.offer.id }) { PostedRideCard(it, onOpenOfferRequests) }
                }
                item { SectionHeader("My posted requests") }
                if (s.requests.isEmpty()) {
                    item { EmptyLine("You haven't posted any requests yet.") }
                } else {
                    items(s.requests, key = { it.id }) { PostedRequestCard(it, onOpenRequestProposals) }
                }
            }
        }
    }
}

@Composable
private fun PostedRideCard(ride: PostedRide, onOpen: (String) -> Unit) {
    val o = ride.offer
    AppCard(onClick = { onOpen(o.id) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${o.origin.cityName}  →  ${o.destination.cityName}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                offerStatusTag(o.status, o.availableSeats)?.let { (label, tone) -> Tag(label, tone) }
                if (ride.pendingCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) { Text("${ride.pendingCount}") }
                }
            }
        }
        Text(formatDateTime(o.departureDate, o.departureTime), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (ride.pendingCount > 0) "${ride.pendingCount} request(s) awaiting your response" else "No pending requests",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

// Owner-facing status chip for one of my offers: Cancelled/Completed by status, else Full/Active by seats.
private fun offerStatusTag(status: String?, availableSeats: Int): Pair<String, Tone>? = when (status?.uppercase()) {
    "CANCELLED" -> "Cancelled" to Tone.Danger
    "COMPLETED" -> "Completed" to Tone.Neutral
    "ACTIVE" -> if (availableSeats <= 0) "Full" to Tone.Warning else "Active" to Tone.Success
    else -> null
}

// Owner-facing status chip for one of my requests.
private fun requestStatusTag(status: String?): Pair<String, Tone>? = when (status?.uppercase()) {
    "CANCELLED" -> "Cancelled" to Tone.Danger
    "FULFILLED" -> "Fulfilled" to Tone.Neutral
    "ACTIVE" -> "Active" to Tone.Success
    else -> null
}

@Composable
private fun PostedRequestCard(request: RequestItem, onOpen: (String) -> Unit) {
    AppCard(onClick = { onOpen(request.id) }) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                "${request.origin.cityName}  →  ${request.destination.cityName}",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                requestStatusTag(request.status)?.let { (label, tone) -> Tag(label, tone) }
                if (request.pendingProposalCount > 0) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ) { Text("${request.pendingProposalCount}") }
                }
            }
        }
        Text(formatDateTime(request.preferredDate, request.preferredTimeWindow), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            if (request.pendingProposalCount > 0) "${request.pendingProposalCount} proposal(s) awaiting your response" else "${request.seatsNeeded} seat(s) needed — tap to see proposals",
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassengerActivity(
    onOpenChat: (String, String) -> Unit,
    bookingsVm: MyBookingsViewModel = hiltViewModel(),
    proposalsVm: MyProposalsViewModel = hiltViewModel(),
) {
    val bookingsState by bookingsVm.uiState.collectAsState()
    val proposalsState by proposalsVm.uiState.collectAsState()
    val cancelling by bookingsVm.cancelling.collectAsState()
    val bookingOpening by bookingsVm.opening.collectAsState()
    val bookingsRefreshing by bookingsVm.refreshing.collectAsState()
    val working by proposalsVm.working.collectAsState()
    val proposalOpening by proposalsVm.opening.collectAsState()
    val proposalsRefreshing by proposalsVm.refreshing.collectAsState()

    LaunchedEffect(Unit) {
        bookingsVm.load()
        proposalsVm.load()
    }
    LaunchedEffect(Unit) { bookingsVm.openChat.collect { onOpenChat(it.conversationId, it.name) } }
    LaunchedEffect(Unit) { proposalsVm.openChat.collect { onOpenChat(it.conversationId, it.name) } }

    PullToRefreshBox(
        isRefreshing = bookingsRefreshing || proposalsRefreshing,
        onRefresh = { bookingsVm.refresh(); proposalsVm.refresh() },
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            item { SectionHeader("My bookings") }
            when (val s = bookingsState) {
                is MyBookingsUiState.Loading -> item { EmptyLine("Loading…") }
                is MyBookingsUiState.Error -> item { EmptyLine(s.message) }
                is MyBookingsUiState.Success ->
                    if (s.bookings.isEmpty()) {
                        item { EmptyLine("You haven't requested any rides yet.") }
                    } else {
                        items(s.bookings, key = { "b-${it.id}" }) {
                            BookingCard(it, cancelling == it.id, bookingOpening == it.id, bookingsVm::cancel, bookingsVm::message)
                        }
                    }
            }

            item { SectionHeader("My proposals") }
            when (val s = proposalsState) {
                is MyProposalsUiState.Loading -> item { EmptyLine("Loading…") }
                is MyProposalsUiState.Error -> item { EmptyLine(s.message) }
                is MyProposalsUiState.Success ->
                    if (s.proposals.isEmpty()) {
                        item { EmptyLine("You haven't proposed on any requests yet.") }
                    } else {
                        items(s.proposals, key = { "p-${it.id}" }) {
                            MyProposalCard(it, working == it.id, proposalOpening == it.id, proposalsVm::withdraw,
                                proposalsVm::report, proposalsVm::block, proposalsVm::message)
                        }
                    }
            }
        }
    }
}

@Composable
private fun CenteredSpinner() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyLine(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

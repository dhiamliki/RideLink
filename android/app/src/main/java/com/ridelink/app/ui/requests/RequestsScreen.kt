package com.ridelink.app.ui.requests

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.Cities
import com.ridelink.app.ui.common.CityDropdown
import com.ridelink.app.ui.common.DateField
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.formatDateTime
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.RideCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    onOpenRequest: (String) -> Unit,
    viewModel: RequestsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        val originCity = Cities.ALL.firstOrNull { it.name == filters.origin?.name }
        val destCity = Cities.ALL.firstOrNull { it.name == filters.destination?.name }
        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.screen),
            verticalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            Text("Ride requests", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                CityDropdown("From", originCity, Cities.ALL, { viewModel.setRoute(it, destCity, filters.date) }, Modifier.weight(1f))
                CityDropdown("To", destCity, Cities.ALL, { viewModel.setRoute(originCity, it, filters.date) }, Modifier.weight(1f))
            }
            DateField("Date", filters.date, onPick = { viewModel.setRoute(originCity, destCity, it) })
            PrimaryButton("Search", onClick = viewModel::load)
        }

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = uiState) {
                is RequestsUiState.Loading -> LoadingState()
                is RequestsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is RequestsUiState.Success ->
                    if (state.requests.isEmpty()) {
                        EmptyState("No ride requests match yet — try a different route or date.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = Dimens.screen, end = Dimens.screen, top = Dimens.lg, bottom = Dimens.fabClearance),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md),
                        ) {
                            items(state.requests, key = { it.id }) { request ->
                                RideCard(
                                    personName = request.passenger?.displayName ?: "Passenger",
                                    route = "${request.origin.cityName}  →  ${request.destination.cityName}",
                                    subtitle = formatDateTime(request.preferredDate, request.preferredTimeWindow),
                                    footerStart = "${request.seatsNeeded} seat(s) needed",
                                    footerEnd = request.maxPricePerSeat?.let { "up to $it DT" },
                                    matchScore = request.matchScore,
                                    onClick = { onOpenRequest(request.id) },
                                )
                            }
                        }
                    }
            }
        }
    }
}

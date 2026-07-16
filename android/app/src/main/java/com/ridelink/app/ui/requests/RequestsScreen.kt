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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.Cities
import com.ridelink.app.data.remote.RequestItem
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.CityDropdown
import com.ridelink.app.ui.common.DateField
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.MatchBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(viewModel: RequestsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        val originCity = Cities.ALL.firstOrNull { it.name == filters.origin?.name }
        val destCity = Cities.ALL.firstOrNull { it.name == filters.destination?.name }
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Ride requests", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CityDropdown("From", originCity, Cities.ALL, { viewModel.setRoute(it, destCity, filters.date) }, Modifier.weight(1f))
                CityDropdown("To", destCity, Cities.ALL, { viewModel.setRoute(originCity, it, filters.date) }, Modifier.weight(1f))
            }
            DateField("Date", filters.date, onPick = { viewModel.setRoute(originCity, destCity, it) })
            Button(onClick = viewModel::load, modifier = Modifier.fillMaxWidth()) { Text("Search") }
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
                        EmptyState("No ride requests found")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.requests, key = { it.id }) { RequestCard(it) }
                        }
                    }
            }
        }
    }
}

@Composable
private fun RequestCard(request: RequestItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Avatar(request.passenger?.displayName)
                    Text(request.passenger?.displayName ?: "Passenger", style = MaterialTheme.typography.titleMedium)
                }
                request.matchScore?.let { MatchBadge(it) }
            }
            Text(
                "${request.origin.cityName}  →  ${request.destination.cityName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${request.preferredDate} · ${request.preferredTimeWindow}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${request.seatsNeeded} seat(s) needed", style = MaterialTheme.typography.bodyMedium)
                request.maxPricePerSeat?.let {
                    Text(
                        "up to $it DT",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

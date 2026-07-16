package com.ridelink.app.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.Cities
import com.ridelink.app.data.remote.OfferItem
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.CityDropdown
import com.ridelink.app.ui.common.DateField
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.MatchBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onOpenOffer: (String) -> Unit,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val filters by viewModel.filters.collectAsState()
    val refreshing by viewModel.refreshing.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    // Reload whenever the feed enters composition (e.g. returning after posting an offer).
    LaunchedEffect(Unit) { viewModel.load() }

    Column(Modifier.fillMaxSize()) {
        SearchBar(
            origin = filters.origin?.name,
            destination = filters.destination?.name,
            date = filters.date,
            onSetRoute = viewModel::setRoute,
            onSearch = viewModel::load,
            onOpenFilters = { showFilters = true },
        )

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = viewModel::refresh,
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = uiState) {
                is FeedUiState.Loading -> LoadingState()
                is FeedUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
                is FeedUiState.Success ->
                    if (state.offers.isEmpty()) {
                        EmptyState("No rides found")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.offers, key = { it.id }) { OfferCard(it, onOpenOffer) }
                        }
                    }
            }
        }
    }

    if (showFilters) {
        FiltersSheet(
            initialMaxPrice = filters.maxPrice,
            initialMinSeats = filters.minSeats,
            onApply = { maxPrice, minSeats ->
                viewModel.applyFilters(maxPrice, minSeats)
                showFilters = false
            },
            onClear = {
                viewModel.clear()
                showFilters = false
            },
            onDismiss = { showFilters = false },
        )
    }
}

@Composable
private fun SearchBar(
    origin: String?,
    destination: String?,
    date: String?,
    onSetRoute: (com.ridelink.app.data.TunisianCity?, com.ridelink.app.data.TunisianCity?, String?) -> Unit,
    onSearch: () -> Unit,
    onOpenFilters: () -> Unit,
) {
    val originCity = Cities.ALL.firstOrNull { it.name == origin }
    val destCity = Cities.ALL.firstOrNull { it.name == destination }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Where are you going?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CityDropdown("From", originCity, Cities.ALL, { onSetRoute(it, destCity, date) }, Modifier.weight(1f))
            CityDropdown("To", destCity, Cities.ALL, { onSetRoute(originCity, it, date) }, Modifier.weight(1f))
        }
        DateField("Date", date, onPick = { onSetRoute(originCity, destCity, it) })
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onSearch, modifier = Modifier.weight(1f)) { Text("Search") }
            OutlinedButton(onClick = onOpenFilters) { Text("Filters") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FiltersSheet(
    initialMaxPrice: Double?,
    initialMinSeats: Int?,
    onApply: (Double?, Int?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    var maxPrice by remember { mutableStateOf(initialMaxPrice?.toString() ?: "") }
    var minSeats by remember { mutableStateOf(initialMinSeats?.toString() ?: "") }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = maxPrice,
                onValueChange = { maxPrice = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Max price per seat (DT)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = minSeats,
                onValueChange = { minSeats = it.filter { c -> c.isDigit() } },
                label = { Text("Min seats") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { onApply(maxPrice.toDoubleOrNull(), minSeats.toIntOrNull()) },
                    modifier = Modifier.weight(1f),
                ) { Text("Apply") }
                OutlinedButton(onClick = onClear) { Text("Clear") }
            }
        }
    }
}

@Composable
private fun OfferCard(offer: OfferItem, onOpenOffer: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenOffer(offer.id) },
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Avatar(offer.driver?.displayName)
                    Text(
                        offer.driver?.displayName ?: "Driver",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                offer.matchScore?.let { MatchBadge(it) }
            }
            Text(
                "${offer.origin.cityName}  →  ${offer.destination.cityName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                "${offer.departureDate} · ${offer.departureTime}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${offer.availableSeats} seat(s) left", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "${offer.pricePerSeat} DT",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

package com.ridelink.app.ui.feed

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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.Cities
import com.ridelink.app.ui.common.CityDropdown
import com.ridelink.app.ui.common.DateField
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.RideCard
import com.ridelink.app.ui.common.SecondaryButton

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
                        EmptyState("No rides match yet — try a different route or date.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
                            verticalArrangement = Arrangement.spacedBy(Dimens.md),
                        ) {
                            items(state.offers, key = { it.id }) { offer ->
                                RideCard(
                                    personName = offer.driver?.displayName ?: "Driver",
                                    route = "${offer.origin.cityName}  →  ${offer.destination.cityName}",
                                    subtitle = "${offer.departureDate} · ${offer.departureTime}",
                                    footerStart = "${offer.availableSeats} seat(s) left",
                                    footerEnd = "${offer.pricePerSeat} DT",
                                    matchScore = offer.matchScore,
                                    onClick = { onOpenOffer(offer.id) },
                                )
                            }
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
            .padding(Dimens.screen),
        verticalArrangement = Arrangement.spacedBy(Dimens.sm),
    ) {
        Text("Where are you going?", style = MaterialTheme.typography.titleLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            CityDropdown("From", originCity, Cities.ALL, { onSetRoute(it, destCity, date) }, Modifier.weight(1f))
            CityDropdown("To", destCity, Cities.ALL, { onSetRoute(originCity, it, date) }, Modifier.weight(1f))
        }
        DateField("Date", date, onPick = { onSetRoute(originCity, destCity, it) })
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
            PrimaryButton("Search", onClick = onSearch, modifier = Modifier.weight(1f))
            SecondaryButton("Filters", onClick = onOpenFilters, modifier = Modifier)
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
                .padding(Dimens.screen),
            verticalArrangement = Arrangement.spacedBy(Dimens.md),
        ) {
            Text("Filters", style = MaterialTheme.typography.titleLarge)
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
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.sm)) {
                PrimaryButton(
                    "Apply",
                    onClick = { onApply(maxPrice.toDoubleOrNull(), minSeats.toIntOrNull()) },
                    modifier = Modifier.weight(1f),
                )
                SecondaryButton("Clear", onClick = onClear, modifier = Modifier)
            }
        }
    }
}

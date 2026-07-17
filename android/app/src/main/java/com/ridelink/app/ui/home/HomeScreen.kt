package com.ridelink.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.SecondaryButton
import com.ridelink.app.ui.common.SectionHeader

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onOpenMyProposals: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loggedOut.collect { onLoggedOut() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Dimens.screen),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg),
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading ->
                CircularProgressIndicator(Modifier.padding(Dimens.xl).align(Alignment.CenterHorizontally))

            is HomeUiState.Success -> {
                val name = state.profile.displayName?.takeIf { it.isNotBlank() } ?: "there"
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
                        Avatar(state.profile.displayName, size = 56)
                        Column(Modifier.weight(1f)) {
                            Text(name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                state.profile.phoneNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    state.profile.bio?.takeIf { it.isNotBlank() }?.let {
                        Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            is HomeUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
                SecondaryButton("Retry", onClick = viewModel::load)
            }
        }

        SectionHeader("Activity")
        PrimaryButton("My bookings", onClick = onOpenMyBookings)
        PrimaryButton("My proposals", onClick = onOpenMyProposals)

        SectionHeader("Account")
        SecondaryButton("Blocked users", onClick = onOpenBlockedUsers)
        SecondaryButton("Log out", onClick = viewModel::logout)
    }
}

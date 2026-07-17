package com.ridelink.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(
    onLoggedOut: () -> Unit,
    onOpenMyBookings: () -> Unit,
    onOpenMyProposals: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loggedOut.collect { onLoggedOut() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        when (val state = uiState) {
            is HomeUiState.Loading -> CircularProgressIndicator()

            is HomeUiState.Success -> {
                val name = state.profile.displayName?.takeIf { it.isNotBlank() } ?: "there"
                Text("Welcome, $name", style = MaterialTheme.typography.headlineMedium)
                Text(state.profile.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                state.profile.bio?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }

            is HomeUiState.Error -> {
                Text(state.message, color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::load) { Text("Retry") }
            }
        }

        Button(
            onClick = onOpenMyBookings,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("My bookings")
        }

        Button(
            onClick = onOpenMyProposals,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("My proposals")
        }

        OutlinedButton(
            onClick = viewModel::logout,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log out")
        }
    }
}

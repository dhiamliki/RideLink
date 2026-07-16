package com.ridelink.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "RideLink", style = MaterialTheme.typography.headlineMedium)

        when (val state = uiState) {
            is HealthUiState.Loading -> CircularProgressIndicator()
            is HealthUiState.Success -> Text(text = "Backend status: ${state.status}")
            is HealthUiState.Error -> Text(
                text = "Backend unreachable: ${state.message}",
                color = MaterialTheme.colorScheme.error,
            )
        }

        Button(onClick = viewModel::checkHealth) {
            Text(text = "Retry")
        }
    }
}

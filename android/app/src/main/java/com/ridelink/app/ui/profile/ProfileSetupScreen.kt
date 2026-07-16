package com.ridelink.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
fun ProfileSetupScreen(
    onDone: () -> Unit,
    viewModel: ProfileSetupViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onDone() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Set up your profile", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(
            value = state.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = { Text("Display name") },
            singleLine = true,
            isError = state.error != null,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.bio,
            onValueChange = viewModel::onBioChange,
            label = { Text("Bio (optional)") },
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Button(
            onClick = viewModel::save,
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.loading) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
            Text("Continue")
        }
    }
}

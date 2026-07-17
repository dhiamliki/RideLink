package com.ridelink.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.PrimaryButton

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
            .padding(Dimens.xl),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Set up your profile", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Tell drivers and passengers who they'll be riding with.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.displayName,
            onValueChange = viewModel::onDisplayNameChange,
            label = { Text("Display name") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            isError = state.error != null,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = state.bio,
            onValueChange = viewModel::onBioChange,
            label = { Text("Bio (optional)") },
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        PrimaryButton("Continue", onClick = viewModel::save, loading = state.loading)
    }
}

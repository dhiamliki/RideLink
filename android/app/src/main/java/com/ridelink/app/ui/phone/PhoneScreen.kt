package com.ridelink.app.ui.phone

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.LaunchedEffect
import com.ridelink.app.ui.common.BrandMark
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.PrimaryButton

@Composable
fun PhoneScreen(
    onCodeSent: (phoneNumber: String, devCode: String?) -> Unit,
    viewModel: PhoneViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.codeSent.collect { target -> onCodeSent(target.phoneNumber, target.devCode) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.xl),
        verticalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BrandMark()
        Text("RideLink", style = MaterialTheme.typography.headlineLarge)
        Text(
            "Share the ride, split the cost.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            value = state.phoneNumber,
            onValueChange = viewModel::onPhoneChange,
            label = { Text("Phone number") },
            placeholder = { Text("+216 12 345 678") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            isError = state.error != null,
            modifier = Modifier.fillMaxWidth(),
        )

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        PrimaryButton("Send code", onClick = viewModel::sendCode, loading = state.loading)
    }
}

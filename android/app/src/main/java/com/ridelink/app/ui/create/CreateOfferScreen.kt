package com.ridelink.app.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.Cities
import com.ridelink.app.ui.common.CityDropdown
import com.ridelink.app.ui.common.DateField
import com.ridelink.app.ui.common.SeatStepper
import com.ridelink.app.ui.common.TimeField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOfferScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreateOfferViewModel = hiltViewModel(),
) {
    val s by viewModel.state.collectAsState()

    LaunchedEffect(Unit) { viewModel.created.collect { onDone() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offer a ride") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CityDropdown("From", s.origin, Cities.ALL, { c -> viewModel.update { it.copy(origin = c) } })
            CityDropdown("To", s.destination, Cities.ALL, { c -> viewModel.update { it.copy(destination = c) } })
            DateField("Departure date", s.date, onPick = { d -> viewModel.update { it.copy(date = d) } })
            TimeField("Departure time", s.time, onPick = { t -> viewModel.update { it.copy(time = t) } })
            SeatStepper("Seats", s.seats, onChange = { v -> viewModel.update { it.copy(seats = v) } })
            OutlinedTextField(
                value = s.price,
                onValueChange = { v -> viewModel.update { st -> st.copy(price = v.filter { c -> c.isDigit() || c == '.' }) } },
                label = { Text("Price per seat (DT)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = s.notes,
                onValueChange = { v -> viewModel.update { it.copy(notes = v) } },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
            )
            ToggleRow("Smoking allowed", s.smokingAllowed) { v -> viewModel.update { it.copy(smokingAllowed = v) } }
            ToggleRow("Pets allowed", s.petsAllowed) { v -> viewModel.update { it.copy(petsAllowed = v) } }

            s.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = viewModel::submit,
                enabled = !s.submitting,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (s.submitting) CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp))
                Text("Post offer")
            }
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

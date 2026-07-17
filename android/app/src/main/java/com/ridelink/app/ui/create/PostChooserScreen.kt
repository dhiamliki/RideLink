package com.ridelink.app.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostChooserScreen(
    onOfferRide: () -> Unit,
    onRequestRide: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post a ride") },
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
                .padding(Dimens.xl),
            verticalArrangement = Arrangement.spacedBy(Dimens.lg, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("What would you like to post?", style = MaterialTheme.typography.titleLarge)
            PrimaryButton("Offer a ride (I'm driving)", onClick = onOfferRide)
            SecondaryButton("Request a ride (I need a seat)", onClick = onRequestRide)
        }
    }
}

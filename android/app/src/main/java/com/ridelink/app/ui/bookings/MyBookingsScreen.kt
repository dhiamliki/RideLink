package com.ridelink.app.ui.bookings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.ridelink.app.data.remote.BookingSummary
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.formatDateTime
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.SecondaryButton
import com.ridelink.app.ui.common.StatusPill

// A single "my booking" row (as a passenger). Rendered by the Activity hub's Passenger section.
@Composable
internal fun BookingCard(
    booking: BookingSummary,
    cancelling: Boolean,
    opening: Boolean,
    onCancel: (String) -> Unit,
    onMessage: (BookingSummary) -> Unit,
) {
    val route = booking.offer?.let { "${it.origin?.cityName ?: "?"}  →  ${it.destination?.cityName ?: "?"}" }
    val status = booking.status.uppercase()
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(route ?: "Ride", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            StatusPill(booking.status)
        }
        booking.offer?.let { o ->
            if (o.departureDate != null) {
                Text(formatDateTime(o.departureDate, o.departureTime), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Text("${booking.seatsBooked} seat(s)", style = MaterialTheme.typography.bodyMedium)

        if (status == "ACCEPTED") {
            ContactCard(booking.counterpartContact?.displayName, booking.counterpartContact?.phoneNumber)
            PrimaryButton(if (opening) "Opening…" else "Message", onClick = { onMessage(booking) }, enabled = !opening)
        }

        if (status == "REQUESTED" || status == "ACCEPTED") {
            SecondaryButton(if (cancelling) "Cancelling…" else "Cancel booking", onClick = { onCancel(booking.id) }, enabled = !cancelling)
        }
    }
}

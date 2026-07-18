package com.ridelink.app.ui.proposals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.ridelink.app.data.remote.Proposal
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.ContactCard
import com.ridelink.app.ui.common.formatRideDate
import com.ridelink.app.ui.common.PrimaryButton
import com.ridelink.app.ui.common.SafetyMenu
import com.ridelink.app.ui.common.SecondaryButton
import com.ridelink.app.ui.common.StatusPill

// A single "my proposal" row (as a driver). Rendered by the Activity hub's Passenger section.
@Composable
internal fun MyProposalCard(
    proposal: Proposal,
    working: Boolean,
    opening: Boolean,
    onWithdraw: (String) -> Unit,
    onReport: (String, String, String?) -> Unit,
    onBlock: (String) -> Unit,
    onMessage: (Proposal) -> Unit,
) {
    val status = proposal.status.uppercase()
    val route = proposal.request?.let { "${it.originCity ?: "?"}  →  ${it.destCity ?: "?"}" }
    // Counterpart on this screen is the request owner; their name arrives via `contact` once ACCEPTED.
    val ownerId = proposal.request?.passengerId
    val ownerName = proposal.contact?.displayName ?: "Passenger"
    AppCard {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(route ?: "Request", style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusPill(proposal.status)
                if (ownerId != null) {
                    SafetyMenu(
                        targetName = ownerName,
                        onReport = { reason, detail -> onReport(ownerId, reason, detail) },
                        onBlock = { onBlock(ownerId) },
                    )
                }
            }
        }
        formatRideDate(proposal.request?.preferredDate)?.let {
            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        proposal.message?.takeIf { it.isNotBlank() }?.let {
            Text("“$it”", style = MaterialTheme.typography.bodyMedium)
        }
        proposal.pricePerSeat?.let {
            Text("Proposed $it DT per seat", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }

        if (status == "ACCEPTED") {
            ContactCard(proposal.contact?.displayName, proposal.contact?.phoneNumber)
            PrimaryButton(if (opening) "Opening…" else "Message", onClick = { onMessage(proposal) }, enabled = !opening)
        }

        if (status == "PROPOSED") {
            SecondaryButton(if (working) "Withdrawing…" else "Withdraw", onClick = { onWithdraw(proposal.id) }, enabled = !working)
        }
    }
}

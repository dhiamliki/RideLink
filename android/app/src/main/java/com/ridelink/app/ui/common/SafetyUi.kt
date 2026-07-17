package com.ridelink.app.ui.common

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

// Backend ReportReason enum values paired with human-readable labels.
val REPORT_REASONS: List<Pair<String, String>> = listOf(
    "HARASSMENT" to "Harassment",
    "UNSAFE_DRIVING" to "Unsafe driving",
    "NO_SHOW" to "No-show",
    "INAPPROPRIATE" to "Inappropriate behaviour",
    "OTHER" to "Other",
)

// Overflow menu (Report / Block) shown wherever another user is visible. Networking is done by the
// caller via onReport/onBlock; this shows the dialogs and confirmation toasts.
@Composable
fun SafetyMenu(
    targetName: String,
    onReport: (reason: String, detail: String?) -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var showReport by remember { mutableStateOf(false) }
    var showBlock by remember { mutableStateOf(false) }

    IconButton(onClick = { menuOpen = true }, modifier = modifier) {
        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(text = { Text("Report") }, onClick = { menuOpen = false; showReport = true })
            DropdownMenuItem(text = { Text("Block") }, onClick = { menuOpen = false; showBlock = true })
        }
    }

    if (showReport) {
        ReportDialog(
            targetName = targetName,
            onDismiss = { showReport = false },
            onSubmit = { reason, detail ->
                showReport = false
                onReport(reason, detail)
                Toast.makeText(context, "Report submitted. Thanks for keeping RideLink safe.", Toast.LENGTH_SHORT).show()
            },
        )
    }
    if (showBlock) {
        BlockDialog(
            targetName = targetName,
            onDismiss = { showBlock = false },
            onConfirm = {
                showBlock = false
                onBlock()
                Toast.makeText(context, "$targetName blocked.", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

@Composable
private fun ReportDialog(
    targetName: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, detail: String?) -> Unit,
) {
    var reason by remember { mutableStateOf(REPORT_REASONS.first().first) }
    var detail by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Report $targetName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Why are you reporting this user?", style = MaterialTheme.typography.bodyMedium)
                REPORT_REASONS.forEach { (value, label) ->
                    Row(
                        Modifier.fillMaxWidth().selectable(selected = reason == value, onClick = { reason = value }),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = reason == value, onClick = { reason = value })
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                OutlinedTextField(
                    value = detail,
                    onValueChange = { detail = it },
                    label = { Text("Details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(reason, detail.trim().ifBlank { null }) }) { Text("Submit") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun BlockDialog(targetName: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Block $targetName?") },
        text = { Text("They won't be able to contact you, and you won't see each other's rides.") },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Block") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

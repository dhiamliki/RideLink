package com.ridelink.app.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ridelink.app.data.TunisianCity
import com.ridelink.app.ui.theme.DangerInk
import com.ridelink.app.ui.theme.DangerTint
import com.ridelink.app.ui.theme.SuccessInk
import com.ridelink.app.ui.theme.SuccessTint
import com.ridelink.app.ui.theme.WarningInk
import com.ridelink.app.ui.theme.WarningTint

// Consistent spacing scale used across the app.
object Dimens {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val screen = 20.dp // default screen edge padding
    val fabClearance = 88.dp // bottom list padding so the FAB never covers the last item
}

// ---- Buttons ----

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.heightIn(min = 52.dp),
    ) {
        if (loading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(18.dp).padding(end = 0.dp),
            )
        } else {
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.heightIn(min = 52.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ---- Segmented toggle (top-of-screen switch, e.g. Offers|Requests, Driver|Passenger) ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

// ---- Card ----

// One card style for the whole app: surface fill, hairline border, soft rounding, no heavy shadow.
@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    val elevation = CardDefaults.cardElevation(defaultElevation = 1.dp, pressedElevation = 1.dp)
    if (onClick != null) {
        Card(onClick = onClick, shape = MaterialTheme.shapes.medium, colors = colors, border = border, elevation = elevation, modifier = modifier.fillMaxWidth()) {
            Column(Modifier.padding(Dimens.lg), verticalArrangement = Arrangement.spacedBy(Dimens.sm), content = content)
        }
    } else {
        Card(shape = MaterialTheme.shapes.medium, colors = colors, border = border, elevation = elevation, modifier = modifier.fillMaxWidth()) {
            Column(Modifier.padding(Dimens.lg), verticalArrangement = Arrangement.spacedBy(Dimens.sm), content = content)
        }
    }
}

// ---- Headers & avatars ----

@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

// Brand logo mark: an indigo rounded square with a bolt glyph, for auth / splash screens.
@Composable
fun BrandMark(size: Int = 72) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(size / 4)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Bolt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size((size * 0.55f).dp),
        )
    }
}

@Composable
fun Avatar(name: String?, size: Int = 44) {
    val initial = name?.trim()?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(size.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun MatchBadge(score: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
            Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
            Text(
                text = "$score% match",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

// ---- States ----

@Composable
fun LoadingState() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(message: String, icon: ImageVector = Icons.Outlined.Inbox) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.xl),
        verticalArrangement = Arrangement.spacedBy(Dimens.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(34.dp))
        }
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.xl),
        verticalArrangement = Arrangement.spacedBy(Dimens.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyLarge)
        OutlinedButton(onClick = onRetry, shape = MaterialTheme.shapes.small) { Text("Retry") }
    }
}

// ---- Status pill (semantic colour by status) ----

@Composable
fun StatusPill(status: String) {
    val (bg, fg) = when (status.uppercase()) {
        "ACCEPTED" -> SuccessTint to SuccessInk
        "REQUESTED", "PROPOSED", "PENDING" -> WarningTint to WarningInk
        "DECLINED", "CANCELLED", "WITHDRAWN" -> DangerTint to DangerInk
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val label = status.lowercase().replaceFirstChar { it.uppercase() }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

// A small coloured dot + label, handy for inline semantic tags (e.g. "Full").
@Composable
fun Tag(text: String, tone: Tone = Tone.Neutral) {
    val (bg, fg) = when (tone) {
        Tone.Success -> SuccessTint to SuccessInk
        Tone.Warning -> WarningTint to WarningInk
        Tone.Danger -> DangerTint to DangerInk
        Tone.Neutral -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(color = bg, contentColor = fg, shape = RoundedCornerShape(50)) {
        Text(text, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

enum class Tone { Success, Warning, Danger, Neutral }

// ---- RideCard: shared card for the feed (offers) and requests tab ----

@Composable
fun RideCard(
    personName: String,
    route: String,
    subtitle: String,
    footerStart: String,
    footerEnd: String? = null,
    matchScore: Int? = null,
    onClick: () -> Unit,
) {
    AppCard(onClick = onClick) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.sm), modifier = Modifier.weight(1f)) {
                Avatar(personName, size = 38)
                Text(
                    personName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            matchScore?.let { MatchBadge(it) }
        }
        Text(
            route,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(Modifier.fillMaxWidth().padding(top = Dimens.xs), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(footerStart, style = MaterialTheme.typography.bodyMedium)
            footerEnd?.let {
                Text(it, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

// ---- Contact reveal (shown once a connection is ACCEPTED) ----

@Composable
fun ContactCard(name: String?, phone: String?) {
    Surface(
        color = SuccessTint,
        contentColor = SuccessInk,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(Dimens.md), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("You're connected — coordinate your trip", style = MaterialTheme.typography.labelMedium)
            name?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            phone?.let { Text(it, style = MaterialTheme.typography.titleMedium) }
        }
    }
}

@Composable
fun SeatStepper(label: String, value: Int, onChange: (Int) -> Unit, min: Int = 1, max: Int = 8) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        OutlinedButton(onClick = { if (value > min) onChange(value - 1) }, enabled = value > min, shape = MaterialTheme.shapes.small) { Text("–") }
        Text("$value", style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { if (value < max) onChange(value + 1) }, enabled = value < max, shape = MaterialTheme.shapes.small) { Text("+") }
    }
}

// ---- Dropdowns (unchanged behaviour) ----

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CityDropdown(
    label: String,
    selected: TunisianCity?,
    cities: List<TunisianCity>,
    onSelect: (TunisianCity) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select a city") },
            shape = MaterialTheme.shapes.small,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            cities.forEach { city ->
                DropdownMenuItem(
                    text = { Text(city.name) },
                    onClick = {
                        onSelect(city)
                        expanded = false
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringDropdown(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            shape = MaterialTheme.shapes.small,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

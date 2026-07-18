package com.ridelink.app.ui.explore

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.SegmentedToggle
import com.ridelink.app.ui.feed.FeedScreen
import com.ridelink.app.ui.requests.RequestsScreen

// The marketplace tab: one screen that toggles between the offer feed and the request feed.
// Search bar, filters and the Post FAB (owned by MainScreen) all stay; only the list swaps.
@Composable
fun ExploreScreen(
    segment: Int,
    onSelectSegment: (Int) -> Unit,
    onOpenOffer: (String) -> Unit,
    onOpenRequest: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        SegmentedToggle(
            options = listOf("Offers", "Requests"),
            selectedIndex = segment,
            onSelect = onSelectSegment,
            modifier = Modifier.padding(horizontal = Dimens.screen, vertical = Dimens.sm),
        )
        // Swapping composables here disposes the hidden list and re-runs its load() on return.
        when (segment) {
            0 -> FeedScreen(onOpenOffer = onOpenOffer)
            else -> RequestsScreen(onOpenRequest = onOpenRequest)
        }
    }
}

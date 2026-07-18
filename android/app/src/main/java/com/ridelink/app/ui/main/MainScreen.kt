package com.ridelink.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.ridelink.app.ui.activity.ActivityScreen
import com.ridelink.app.ui.chat.ConversationsScreen
import com.ridelink.app.ui.chat.ConversationsUiState
import com.ridelink.app.ui.chat.ConversationsViewModel
import com.ridelink.app.ui.explore.ExploreScreen
import com.ridelink.app.ui.home.HomeScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    exploreSegment: Int,
    onSelectExploreSegment: (Int) -> Unit,
    onPost: () -> Unit,
    onOpenOffer: (String) -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenOfferRequests: (String) -> Unit,
    onOpenRequestProposals: (String) -> Unit,
    onOpenChat: (conversationId: String, counterpartName: String) -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onLoggedOut: () -> Unit,
    conversationsViewModel: ConversationsViewModel = hiltViewModel(),
) {
    // The Messages tab badge reflects total unread across conversations. Reload whenever this
    // screen resumes (returning from a chat) and on tab switches so the count stays fresh.
    val convState by conversationsViewModel.uiState.collectAsState()
    val unread = (convState as? ConversationsUiState.Success)?.conversations?.sumOf { it.unreadCount } ?: 0
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { conversationsViewModel.load() }

    val title = when (selectedTab) {
        1 -> "Activity"
        2 -> "Messages"
        3 -> "Profile"
        else -> "RideLink"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { onSelectTab(0) },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Explore") },
                    label = { Text("Explore") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { onSelectTab(1) },
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Activity") },
                    label = { Text("Activity") },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { onSelectTab(2) },
                    icon = {
                        BadgedBox(badge = {
                            if (unread > 0) Badge { Text(if (unread > 99) "99+" else "$unread") }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Messages")
                        }
                    },
                    label = { Text("Messages") },
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { onSelectTab(3) },
                    icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(onClick = onPost) {
                    Icon(Icons.Filled.Add, contentDescription = "Post a ride")
                }
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> ExploreScreen(
                    segment = exploreSegment,
                    onSelectSegment = onSelectExploreSegment,
                    onOpenOffer = onOpenOffer,
                    onOpenRequest = onOpenRequest,
                )
                1 -> ActivityScreen(
                    onOpenOfferRequests = onOpenOfferRequests,
                    onOpenRequestProposals = onOpenRequestProposals,
                    onOpenChat = onOpenChat,
                )
                2 -> ConversationsScreen(
                    onOpenChat = onOpenChat,
                    viewModel = conversationsViewModel,
                )
                3 -> HomeScreen(
                    onLoggedOut = onLoggedOut,
                    onOpenBlockedUsers = onOpenBlockedUsers,
                )
            }
        }
    }
}

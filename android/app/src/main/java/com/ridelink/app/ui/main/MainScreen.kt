package com.ridelink.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.Modifier
import com.ridelink.app.ui.feed.FeedScreen
import com.ridelink.app.ui.home.HomeScreen
import com.ridelink.app.ui.requests.RequestsScreen

private data class Tab(val label: String, val icon: @Composable () -> Unit)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit,
    onPost: () -> Unit,
    onOpenOffer: (String) -> Unit,
    onOpenRequest: (String) -> Unit,
    onOpenMyBookings: () -> Unit,
    onOpenMyProposals: () -> Unit,
    onOpenBlockedUsers: () -> Unit,
    onLoggedOut: () -> Unit,
) {
    val tabs = listOf(
        Tab("Home") { Icon(Icons.Filled.Home, contentDescription = "Home") },
        Tab("Requests") { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Requests") },
        Tab("Profile") { Icon(Icons.Filled.Person, contentDescription = "Profile") },
    )
    val title = when (selectedTab) {
        1 -> "Requests"
        2 -> "Profile"
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
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { onSelectTab(index) },
                        icon = tab.icon,
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onPost) {
                Icon(Icons.Filled.Add, contentDescription = "Post a ride")
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> FeedScreen(onOpenOffer = onOpenOffer)
                1 -> RequestsScreen(onOpenRequest = onOpenRequest)
                2 -> HomeScreen(
                    onLoggedOut = onLoggedOut,
                    onOpenMyBookings = onOpenMyBookings,
                    onOpenMyProposals = onOpenMyProposals,
                    onOpenBlockedUsers = onOpenBlockedUsers,
                )
            }
        }
    }
}

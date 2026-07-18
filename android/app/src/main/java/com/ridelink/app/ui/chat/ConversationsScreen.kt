package com.ridelink.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Badge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.remote.Conversation
import com.ridelink.app.ui.common.AppCard
import com.ridelink.app.ui.common.Avatar
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState

// Rendered as the Messages tab inside MainScreen, which supplies the top bar and shares the
// ViewModel so the tab's unread badge and this list stay in sync. Reloading is driven by MainScreen.
@Composable
fun ConversationsScreen(
    onOpenChat: (conversationId: String, counterpartName: String) -> Unit,
    viewModel: ConversationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ConversationsUiState.Loading -> LoadingState()
            is ConversationsUiState.Error -> ErrorState(state.message, onRetry = viewModel::load)
            is ConversationsUiState.Success ->
                if (state.conversations.isEmpty()) {
                    EmptyState("No messages yet — start a chat from an accepted ride.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = Dimens.screen, vertical = Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.md),
                    ) {
                        items(state.conversations, key = { it.id }) { convo ->
                            ConversationRow(convo, onOpenChat)
                        }
                    }
                }
        }
    }
}

@Composable
private fun ConversationRow(convo: Conversation, onOpenChat: (String, String) -> Unit) {
    val name = convo.counterpart?.displayName?.takeIf { it.isNotBlank() } ?: "RideLink user"
    AppCard(onClick = { onOpenChat(convo.id, name) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Dimens.md)) {
            Avatar(name, size = 44)
            Column(Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    convo.lastMessage?.content?.takeIf { it.isNotBlank() } ?: "No messages yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = if (convo.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                )
            }
            if (convo.unreadCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Text("${convo.unreadCount}") }
            }
        }
    }
}

package com.ridelink.app.ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ridelink.app.data.chat.ChatConnectionState
import com.ridelink.app.data.remote.ChatMessage
import com.ridelink.app.ui.common.Dimens
import com.ridelink.app.ui.common.EmptyState
import com.ridelink.app.ui.common.ErrorState
import com.ridelink.app.ui.common.LoadingState
import com.ridelink.app.ui.common.formatTimestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    counterpartName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val messages by viewModel.messages.collectAsState()
    val history by viewModel.history.collectAsState()
    val myId by viewModel.myId.collectAsState()
    val connection by viewModel.connectionState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(counterpartName)
                        val label = when (connection) {
                            ChatConnectionState.Connecting -> "Connecting…"
                            ChatConnectionState.Reconnecting -> "Reconnecting…"
                            ChatConnectionState.Disconnected -> "Offline"
                            ChatConnectionState.Connected -> null
                        }
                        if (label != null) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (val state = history) {
                    is ChatHistoryState.Loading -> LoadingState()
                    is ChatHistoryState.Error -> ErrorState(state.message, onRetry = viewModel::loadHistory)
                    is ChatHistoryState.Ready ->
                        if (messages.isEmpty()) {
                            EmptyState("Say hello 👋")
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                reverseLayout = true, // newest-first list renders bottom-up
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(Dimens.lg),
                                verticalArrangement = Arrangement.spacedBy(Dimens.sm),
                            ) {
                                items(messages, key = { it.id ?: it.sentAt ?: it.hashCode().toString() }) { msg ->
                                    MessageBubble(msg, mine = msg.senderId != null && msg.senderId == myId)
                                }
                            }
                        }
                }
            }
            MessageInput(onSend = viewModel::send)
        }
    }
}

@Composable
private fun MessageBubble(msg: ChatMessage, mine: Boolean) {
    val bubbleColor = if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val shape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = if (mine) 16.dp else 4.dp,
        bottomEnd = if (mine) 4.dp else 16.dp,
    )
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Surface(color = bubbleColor, contentColor = textColor, shape = shape, modifier = Modifier.widthIn(max = 300.dp)) {
            Column(Modifier.padding(horizontal = Dimens.md, vertical = Dimens.sm)) {
                Text(msg.content, style = MaterialTheme.typography.bodyLarge)
                val time = formatTimestamp(msg.sentAt)
                if (time.isNotBlank()) {
                    Text(
                        time,
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.align(Alignment.End),
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageInput(onSend: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    val submit = {
        if (text.isNotBlank()) {
            onSend(text)
            text = ""
        }
    }
    Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = Dimens.md, vertical = Dimens.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.sm),
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") },
                shape = MaterialTheme.shapes.large,
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            )
            IconButton(onClick = submit, enabled = text.isNotBlank()) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

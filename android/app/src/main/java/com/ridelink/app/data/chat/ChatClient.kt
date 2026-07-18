package com.ridelink.app.data.chat

import com.google.gson.Gson
import com.ridelink.app.data.local.TokenStore
import com.ridelink.app.data.remote.ChatMessage
import com.ridelink.app.data.remote.ReadBody
import com.ridelink.app.data.remote.SendMessageBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.OkHttpClient
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.StompSession
import org.hildan.krossbow.stomp.sendText
import org.hildan.krossbow.stomp.subscribeText
import org.hildan.krossbow.websocket.okhttp.OkHttpWebSocketClient

enum class ChatConnectionState { Disconnected, Connecting, Connected, Reconnecting }

// 10.0.2.2 is the emulator's alias for the host's localhost, where the backend WebSocket lives.
private const val WS_URL = "ws://10.0.2.2:8080/ws"

// Single STOMP session shared by the open chat screen. Connects lazily with the stored JWT (sent as
// the CONNECT "Authorization" header, matching the backend's StompAuthChannelInterceptor), exposes
// incoming messages as a Flow that transparently reconnects on a transient drop, and is disconnected
// when the chat screen closes so no socket is leaked.
@Singleton
class ChatClient @Inject constructor(
    okHttpClient: OkHttpClient,
    private val tokenStore: TokenStore,
    private val gson: Gson,
) {
    // A ping keeps the socket alive through NAT/idle timeouts; newBuilder reuses the shared pool.
    private val stompClient = StompClient(
        OkHttpWebSocketClient(okHttpClient.newBuilder().pingInterval(20, TimeUnit.SECONDS).build()),
    )
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile
    private var session: StompSession? = null

    private val _state = MutableStateFlow(ChatConnectionState.Disconnected)
    val state: StateFlow<ChatConnectionState> = _state.asStateFlow()

    private suspend fun session(): StompSession = mutex.withLock {
        session ?: run {
            if (_state.value != ChatConnectionState.Reconnecting) _state.value = ChatConnectionState.Connecting
            val token = tokenStore.accessToken()
            val headers = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
            val fresh = stompClient.connect(url = WS_URL, customStompConnectHeaders = headers)
            session = fresh
            _state.value = ChatConnectionState.Connected
            fresh
        }
    }

    // Live messages on a conversation topic. Reconnects with backoff on a transient drop; the
    // collector (the chat ViewModel) cancelling this flow is what ultimately tears the socket down.
    fun incoming(conversationId: String): Flow<ChatMessage> = flow {
        emitAll(session().subscribeText("/topic/conversations/$conversationId"))
    }.map { gson.fromJson(it, ChatMessage::class.java) }
        .retryWhen { _, attempt ->
            _state.value = ChatConnectionState.Reconnecting
            session = null
            kotlinx.coroutines.delay((1000L * (attempt + 1)).coerceAtMost(10_000L))
            true
        }

    suspend fun send(conversationId: String, content: String) {
        session().sendText("/app/chat.send", gson.toJson(SendMessageBody(conversationId, content)))
    }

    suspend fun markRead(conversationId: String) {
        runCatching { session().sendText("/app/chat.read", gson.toJson(ReadBody(conversationId))) }
    }

    // Fire-and-forget teardown for use from ViewModel.onCleared (which cannot suspend).
    fun disconnectAsync() {
        scope.launch { disconnect() }
    }

    private suspend fun disconnect() = mutex.withLock {
        runCatching { session?.disconnect() }
        session = null
        _state.value = ChatConnectionState.Disconnected
    }
}

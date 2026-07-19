package com.ridelink.app.data.chat

import com.google.gson.Gson
import com.ridelink.app.data.local.TokenStore
import com.ridelink.app.data.remote.ChatMessage
import com.ridelink.app.data.remote.ReadBody
import com.ridelink.app.data.remote.SendMessageBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
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

// Single STOMP session shared by whichever chat screens are open. Connects lazily with the stored JWT
// (sent as the CONNECT "Authorization" header, matching the backend's StompAuthChannelInterceptor) and
// exposes each conversation's live messages as a Flow.
//
// The connection lifecycle is driven by the number of *active subscribers* (each collector of
// incoming()), NOT by any single screen's ViewModel.onCleared. This is deliberate: the earlier design
// tore the shared session down from ChatViewModel.onCleared, so navigating away from one chat while
// (re)entering another — the common pattern once Conversations became a tab — let a departing screen's
// fire-and-forget disconnect close the very session the entering screen had just subscribed on, leaving
// its subscription attached to a dead socket. Reference counting ties teardown to the last collector
// leaving, so we never kill a connection another open screen still needs.
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

    private var session: StompSession? = null
    private var subscribers = 0

    private val _state = MutableStateFlow(ChatConnectionState.Disconnected)
    val state: StateFlow<ChatConnectionState> = _state.asStateFlow()

    // Returns the live session, connecting if needed. Callers must hold [mutex].
    private suspend fun connectedSession(): StompSession = session ?: run {
        if (_state.value != ChatConnectionState.Reconnecting) _state.value = ChatConnectionState.Connecting
        val token = tokenStore.accessToken()
        val headers = if (token != null) mapOf("Authorization" to "Bearer $token") else emptyMap()
        val fresh = stompClient.connect(url = WS_URL, customStompConnectHeaders = headers)
        session = fresh
        _state.value = ChatConnectionState.Connected
        fresh
    }

    private suspend fun session(): StompSession = mutex.withLock { connectedSession() }

    // Closes the current session (if any) so the next session() reconnects. Callers must hold [mutex].
    private suspend fun dropSession() {
        runCatching { session?.disconnect() }
        session = null
    }

    // Live messages on a conversation topic. Opening a chat starts one collector: it connects (if this
    // is the first subscriber) and SUBSCRIBEs; leaving cancels the collector, which releases the
    // subscriber and disconnects only when it was the last one. On a transient drop the session is
    // dropped and the whole chain restarts — reconnecting AND re-SUBSCRIBEing — with capped backoff, so
    // "Reconnecting…" recovers on its own once the backend is reachable again.
    fun incoming(conversationId: String): Flow<ChatMessage> = flow {
        emitAll(session().subscribeText("/topic/conversations/$conversationId"))
    }.map { gson.fromJson(it, ChatMessage::class.java) }
        .retryWhen { cause, attempt ->
            if (cause is CancellationException) return@retryWhen false
            mutex.withLock { dropSession() }
            _state.value = ChatConnectionState.Reconnecting
            delay((1000L * (attempt + 1)).coerceAtMost(10_000L))
            true
        }
        .onStart { acquire() }
        .onCompletion { release() }

    private suspend fun acquire() = mutex.withLock { subscribers++ }

    private suspend fun release() = mutex.withLock {
        subscribers--
        if (subscribers <= 0) {
            subscribers = 0
            dropSession()
            _state.value = ChatConnectionState.Disconnected
        }
    }

    suspend fun send(conversationId: String, content: String) {
        session().sendText("/app/chat.send", gson.toJson(SendMessageBody(conversationId, content)))
    }

    suspend fun markRead(conversationId: String) {
        runCatching { session().sendText("/app/chat.read", gson.toJson(ReadBody(conversationId))) }
    }
}

package com.ridelink.app.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

// Broadcasts forced-logout events (e.g. refresh failed) so the UI can route back to login.
@Singleton
class SessionManager @Inject constructor() {

    private val _loggedOut = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val loggedOut: SharedFlow<Unit> = _loggedOut

    fun notifyLoggedOut() {
        _loggedOut.tryEmit(Unit)
    }
}

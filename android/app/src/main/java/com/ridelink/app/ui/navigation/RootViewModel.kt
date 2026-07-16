package com.ridelink.app.ui.navigation

import androidx.lifecycle.ViewModel
import com.ridelink.app.data.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharedFlow

// Surfaces the app-wide forced-logout signal (emitted by the auth interceptor when a refresh
// fails) so the navigation host can return to the phone screen.
@HiltViewModel
class RootViewModel @Inject constructor(sessionManager: SessionManager) : ViewModel() {
    val loggedOut: SharedFlow<Unit> = sessionManager.loggedOut
}

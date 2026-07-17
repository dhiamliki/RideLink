package com.ridelink.app.data

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// App-scoped signal so an action on one screen (e.g. blocking a user) can tell the browse lists to
// reload, since they live on a retained back-stack entry and won't re-run their initial load.
@Singleton
class RefreshBus @Inject constructor() {

    private val _browse = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val browse: SharedFlow<Unit> = _browse.asSharedFlow()

    fun refreshBrowse() {
        _browse.tryEmit(Unit)
    }
}

package org.mtier.timetracker.fakes

import android.app.Activity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.mtier.timetracker.data.auth.SsoEvent
import org.mtier.timetracker.data.auth.SsoLoginManager

class FakeSsoLoginManager : SsoLoginManager {
    private val _events = MutableSharedFlow<SsoEvent>(replay = 1, extraBufferCapacity = 1)
    override val events: SharedFlow<SsoEvent> = _events.asSharedFlow()

    var filesAppInstalled = false

    fun emit(event: SsoEvent) {
        _events.tryEmit(event)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun consumeEvent() = _events.resetReplayCache()

    override fun isFilesAppInstalled(): Boolean = filesAppInstalled

    override fun pickAccount(activity: Activity) = Unit
}

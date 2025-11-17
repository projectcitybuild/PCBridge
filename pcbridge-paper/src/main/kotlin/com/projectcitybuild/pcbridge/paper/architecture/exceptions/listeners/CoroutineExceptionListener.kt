package com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CoroutineExceptionListener(
    private val errorTracker: ErrorTracker,
): Listener {
    @EventHandler
    fun onException(event: MCCoroutineExceptionEvent) {
        errorTracker.report(event.exception)
    }
}
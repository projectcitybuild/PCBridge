package com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.pcbridge.paper.core.libs.errors.ErrorReporter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class CoroutineExceptionListener(
    private val errorReporter: ErrorReporter,
): Listener {
    @EventHandler
    fun onException(event: MCCoroutineExceptionEvent) {
        errorReporter.report(event.exception)
    }
}
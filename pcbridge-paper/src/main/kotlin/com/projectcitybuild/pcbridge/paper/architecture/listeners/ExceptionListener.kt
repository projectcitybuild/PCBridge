package com.projectcitybuild.pcbridge.paper.architecture.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.pcbridge.paper.core.libs.errors.SentryReporter
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ExceptionListener(
    private val sentryReporter: SentryReporter,
): Listener {
    @EventHandler
    fun onException(event: MCCoroutineExceptionEvent) {
        sentryReporter.report(event.exception)
    }
}
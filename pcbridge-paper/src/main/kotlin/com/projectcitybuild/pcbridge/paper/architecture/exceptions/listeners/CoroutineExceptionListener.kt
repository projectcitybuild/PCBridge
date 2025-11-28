package com.projectcitybuild.pcbridge.paper.architecture.exceptions.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

class CoroutineExceptionListener(
    private val plugin: JavaPlugin,
    private val errorTracker: ErrorTracker,
): Listener {
    /**
     * Uncaught exceptions during execution in a coroutine bubble
     * up to here. This lets us report it if it hasn't already been
     * reported and consumed.
     */
    @EventHandler
    fun onException(event: MCCoroutineExceptionEvent) {
        // This event reports unhandled exceptions from all plugins using
        // MCCoroutine, so we need to check it actually originated from us
        // https://shynixn.github.io/MCCoroutine/wiki/site/exception/
        if (event.plugin.name != plugin.name) return

        errorTracker.report(event.exception)
    }
}
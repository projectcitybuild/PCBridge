package com.projectcitybuild.plugin.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import javax.inject.Inject

class ExceptionListener @Inject constructor(
    private val errorReporter: ErrorReporter,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChatEvent(event: MCCoroutineExceptionEvent) {
        errorReporter.report(event.exception)
    }
}

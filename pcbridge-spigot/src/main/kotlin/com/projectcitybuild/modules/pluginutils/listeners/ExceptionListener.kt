package com.projectcitybuild.modules.pluginutils.listeners

import com.github.shynixn.mccoroutine.bukkit.MCCoroutineExceptionEvent
import com.projectcitybuild.libs.errorreporting.ErrorReporter
import com.projectcitybuild.support.spigot.listeners.SpigotListener

// TODO: move this to somewhere more appropriate
class ExceptionListener(
    private val errorReporter: ErrorReporter,
) : SpigotListener<MCCoroutineExceptionEvent> {

    override suspend fun handle(event: MCCoroutineExceptionEvent) {
        errorReporter.report(event.exception)
    }
}

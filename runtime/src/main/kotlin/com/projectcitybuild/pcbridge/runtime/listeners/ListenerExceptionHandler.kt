package com.projectcitybuild.pcbridge.runtime.listeners

import com.projectcitybuild.pcbridge.core.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import kotlinx.coroutines.CancellationException
import org.bukkit.event.Event

suspend fun Event.runListenerSafely(
    errorTracker: ErrorTracker,
    failureMessage: String,
    onFailure: suspend (Exception) -> Unit = {},
    block: suspend () -> Unit,
) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        reportListenerFailure(errorTracker, failureMessage, e)
        onFailure(e)
    }
}

fun Event.runListenerSafelySync(
    errorTracker: ErrorTracker,
    failureMessage: String,
    onFailure: (Exception) -> Unit = {},
    block: () -> Unit,
) {
    try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        reportListenerFailure(errorTracker, failureMessage, e)
        onFailure(e)
    }
}

private fun Event.reportListenerFailure(
    errorTracker: ErrorTracker,
    failureMessage: String,
    exception: Exception,
) {
    logSync.warn(exception) { "$failureMessage (event=$eventName)" }
    errorTracker.reportSafely(exception)
}

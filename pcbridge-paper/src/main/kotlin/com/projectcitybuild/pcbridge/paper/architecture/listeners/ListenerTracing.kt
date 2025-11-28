package com.projectcitybuild.pcbridge.paper.architecture.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing.Tracer
import io.opentelemetry.api.common.Attributes
import org.bukkit.event.Event
import org.bukkit.event.Listener

suspend fun <T: Listener> Event.scoped(
    tracer: Tracer,
    listener: Class<T>,
    block: suspend () -> Unit,
) {
    val attributes = Attributes.builder()
        .put("event", eventName)

    tracer.trace("listener.${listener.simpleName}", attributes.build()) {
        runCatching { block() }.onFailure { e ->
            log.error(e, "Listener failed to handle event: $eventName", mapOf(
                "event" to this,
            ))
        }
    }
}

fun <T: Listener> Event.scopedSync(
    tracer: Tracer,
    listener: Class<T>,
    block: () -> Unit,
) {
    val attributes = Attributes.builder()
        .put("event", eventName)

    tracer.traceSync("listener.${listener.simpleName}", attributes.build()) {
        runCatching { block() }.onFailure { e ->
            logSync.error(e, "Listener failed to handle event: $eventName", mapOf(
                "event" to this,
            ))
        }
    }
}
package com.projectcitybuild.pcbridge.runtime.listeners

import com.projectcitybuild.pcbridge.core.observability.tracing.Tracer
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import org.bukkit.event.Event
import org.bukkit.event.Listener

suspend fun <T : Listener> Event.traced(
    tracer: Tracer,
    listener: Class<T>,
    block: suspend () -> Unit,
) {
    val attributes =
        Attributes.builder()
            .put("event", eventName)

    tracer.trace(
        operation = "listener.${listener.simpleName}",
        spanKind = SpanKind.SERVER,
        attributes = attributes.build(),
    ) {
        block()
    }
}

fun <T : Listener> Event.tracedSync(
    tracer: Tracer,
    listener: Class<T>,
    block: () -> Unit,
) {
    val attributes =
        Attributes.builder()
            .put("event", eventName)

    tracer.traceSync(
        operation = "listener.${listener.simpleName}",
        spanKind = SpanKind.SERVER,
        attributes = attributes.build(),
    ) {
        block()
    }
}

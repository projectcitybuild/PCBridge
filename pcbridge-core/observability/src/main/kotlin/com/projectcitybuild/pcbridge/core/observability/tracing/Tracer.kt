package com.projectcitybuild.pcbridge.core.observability.tracing

import io.opentelemetry.api.trace.SpanKind as OtelSpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import io.opentelemetry.api.common.Attributes as OtelAttributes
import kotlinx.coroutines.withContext

class Tracer(
    private val name: String,
    private val otel: OpenTelemetryProvider,
) {
    suspend fun <T> trace(
        operation: String,
        spanKind: SpanKind = SpanKind.INTERNAL,
        attributes: Attributes? = null,
        block: suspend () -> T,
    ): T {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer.spanBuilder(operation)
            .setSpanKind(spanKind.toOtel())
            .apply { if (attributes != null) setAllAttributes(attributes.toOtel()) }
            .startSpan()

        val otelContext = Context.current().with(span)

        try {
            return withContext(otelContext.asContextElement()) {
                block()
            }
        } catch (e: Exception) {
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }

    fun <T> traceSync(
        operation: String,
        spanKind: SpanKind = SpanKind.INTERNAL,
        attributes: Attributes? = null,
        block: () -> T,
    ): T {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer.spanBuilder(operation)
            .setSpanKind(spanKind.toOtel())
            .apply { if (attributes != null) setAllAttributes(attributes.toOtel()) }
            .startSpan()

        try {
            return block()
        } catch (e: Exception) {
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }
}

fun Attributes.toOtel(): OtelAttributes {
    val builder = OtelAttributes.builder()
    for ((key, value) in this) {
        when (value) {
            null -> Unit
            is String -> builder.put(key, value)
            is Long -> builder.put(key, value)
            is Int -> builder.put(key, value.toLong())
            is Double -> builder.put(key, value)
            is Float -> builder.put(key, value.toDouble())
            is Boolean -> builder.put(key, value)
            else -> error("Unsupported attribute type: ${value::class}")
        }
    }
    return builder.build()
}

private fun SpanKind.toOtel() = when (this) {
    SpanKind.INTERNAL -> OtelSpanKind.INTERNAL
    SpanKind.SERVER -> OtelSpanKind.SERVER
    SpanKind.CLIENT -> OtelSpanKind.CLIENT
    SpanKind.PRODUCER -> OtelSpanKind.PRODUCER
    SpanKind.CONSUMER -> OtelSpanKind.CONSUMER
}
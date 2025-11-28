package com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import io.opentelemetry.api.trace.SpanBuilder
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.withContext

class Tracer(
    private val name: String,
    private val otel: OpenTelemetryProvider,
) {
    suspend fun <T> trace(
        operation: String,
        attributes: Map<String, *>? = null,
        block: suspend () -> T,
    ) {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer.spanBuilder(operation)
            .setSpanKind(SpanKind.INTERNAL)
            .setAttributes(attributes)
            .startSpan()

        val otelContext = Context.current().with(span)

        try {
            withContext(otelContext.asContextElement()) {
                block()
            }
        } catch (e: Exception) {
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }
}

private fun SpanBuilder.setAttributes(attributes: Map<String, *>?): SpanBuilder {
    attributes?.forEach { attribute ->
        val key = attribute.key
        when (val value = attribute.value) {
            is String -> setAttribute(key, value)
            is Boolean -> setAttribute(key, value)
            is Long -> setAttribute(key, value)
            is Double -> setAttribute(key, value)
            else -> logSync.e("Unsupported attribute value type for $key")
        }
    }
    return this
}
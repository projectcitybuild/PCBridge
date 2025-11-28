package com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing

import io.opentelemetry.api.common.Attributes
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
        attributes: Attributes? = null,
        block: suspend () -> T,
    ) {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer.spanBuilder(operation)
            .setSpanKind(SpanKind.INTERNAL)
            .apply { if (attributes != null) setAllAttributes(attributes) }
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

    fun <T> traceSync(
        operation: String,
        attributes: Attributes? = null,
        block: () -> T,
    ) {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer.spanBuilder(operation)
            .setSpanKind(SpanKind.INTERNAL)
            .apply { if (attributes != null) setAllAttributes(attributes) }
            .startSpan()

        try {
            block()
        } catch (e: Exception) {
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }
}
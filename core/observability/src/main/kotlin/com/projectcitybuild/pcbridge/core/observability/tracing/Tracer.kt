package com.projectcitybuild.pcbridge.core.observability.tracing

import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.context.Context
import io.opentelemetry.extension.kotlin.asContextElement
import kotlinx.coroutines.CancellationException
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

        val span = tracer
            .spanBuilder(operation)
            .setSpanKind(spanKind)
            .apply { if (attributes != null) setAllAttributes(attributes) }
            .startSpan()

        val otelContext = Context.current().with(span)

        try {
            return withContext(otelContext.asContextElement()) {
                try {
                    block()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    span.recordException(e)
                    throw e
                }
            }
        } finally {
            span.end()
        }
    }

    fun <T> traceSync(
        operation: String,
        spanKind: SpanKind = SpanKind.INTERNAL,
        attributes: Attributes? = null,
        block: (Span) -> T,
    ): T {
        val tracer = otel.sdk.getTracer(name)

        val span = tracer
            .spanBuilder(operation)
            .setSpanKind(spanKind)
            .apply { if (attributes != null) setAllAttributes(attributes) }
            .startSpan()

        try {
            return block(span)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            span.recordException(e)
            throw e
        } finally {
            span.end()
        }
    }
}

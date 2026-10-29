package com.projectcitybuild.pcbridge.core.observability.tracing

import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.common.CompletableResultCode
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.export.SpanExporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.concurrent.CopyOnWriteArrayList

class TracerTest {
    @Test
    fun `suspending trace ends span and preserves context across suspension`() = runBlocking {
        val fixture = createFixture()
        val attributes = Attributes.builder().put("event", "join").build()

        fixture.tracer.trace(
            operation = "listener.PlayerJoin",
            spanKind = SpanKind.SERVER,
            attributes = attributes,
        ) {
            assertTrue(Span.current().spanContext.isValid)
            yield()
            assertTrue(Span.current().spanContext.isValid)
        }

        val span = fixture.exporter.spans.single()
        assertEquals("listener.PlayerJoin", span.name)
        assertEquals(SpanKind.SERVER, span.kind)
        assertEquals("join", span.attributes.get(AttributeKey.stringKey("event")))
        assertTrue(span.endEpochNanos > 0)
    }

    @Test
    fun `suspending trace records and rethrows ordinary exceptions`() {
        val fixture = createFixture()
        val failure = IllegalStateException("failure")

        val thrown = assertThrows<IllegalStateException> {
            runBlocking {
                fixture.tracer.trace("command.test") {
                    throw failure
                }
            }
        }

        assertEquals(failure::class, thrown::class)
        assertEquals(failure.message, thrown.message)
        assertTrue(
            fixture.exporter.spans
                .single()
                .events
                .any { it.name == "exception" },
        )
    }

    @Test
    fun `suspending trace propagates cancellation without recording it`() {
        val fixture = createFixture()
        val cancellation = CancellationException("cancelled")

        val thrown = assertThrows<CancellationException> {
            runBlocking {
                fixture.tracer.trace("command.test") {
                    throw cancellation
                }
            }
        }

        assertEquals(cancellation::class, thrown::class)
        assertEquals(cancellation.message, thrown.message)
        assertTrue(
            fixture.exporter.spans
                .single()
                .events
                .none { it.name == "exception" },
        )
    }

    @Test
    fun `synchronous trace ends span and records ordinary exceptions`() {
        val fixture = createFixture()
        val failure = IllegalArgumentException("failure")

        val thrown = assertThrows<IllegalArgumentException> {
            fixture.tracer.traceSync("listener.Test") {
                throw failure
            }
        }

        assertSame(failure, thrown)
        assertTrue(
            fixture.exporter.spans
                .single()
                .events
                .any { it.name == "exception" },
        )
    }

    @Test
    fun `synchronous trace propagates cancellation without recording it`() {
        val fixture = createFixture()
        val cancellation = CancellationException("cancelled")

        val thrown = assertThrows<CancellationException> {
            fixture.tracer.traceSync("listener.Test") {
                throw cancellation
            }
        }

        assertSame(cancellation, thrown)
        assertTrue(
            fixture.exporter.spans
                .single()
                .events
                .none { it.name == "exception" },
        )
    }

    @Test
    fun `synchronous trace passes the created span to the block`() {
        val fixture = createFixture()

        val result =
            fixture.tracer.traceSync("listener.Test") { span ->
                assertTrue(span.spanContext.isValid)
                "complete"
            }

        assertEquals("complete", result)
        assertEquals(
            "listener.Test",
            fixture.exporter.spans
                .single()
                .name,
        )
    }

    private fun createFixture(): Fixture {
        val exporter = CapturingSpanExporter()
        val tracerProvider = SdkTracerProvider
            .builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build()

        val sdk = OpenTelemetrySdk
            .builder()
            .setTracerProvider(tracerProvider)
            .build()

        val provider = mock<OpenTelemetryProvider>()
        whenever(provider.sdk).thenReturn(sdk)

        return Fixture(Tracer("test", provider), exporter)
    }

    private data class Fixture(
        val tracer: Tracer,
        val exporter: CapturingSpanExporter,
    )

    private class CapturingSpanExporter : SpanExporter {
        val spans = CopyOnWriteArrayList<SpanData>()

        override fun export(spans: Collection<SpanData>): CompletableResultCode {
            this.spans.addAll(spans)
            return CompletableResultCode.ofSuccess()
        }

        override fun flush(): CompletableResultCode = CompletableResultCode.ofSuccess()

        override fun shutdown(): CompletableResultCode = CompletableResultCode.ofSuccess()
    }
}

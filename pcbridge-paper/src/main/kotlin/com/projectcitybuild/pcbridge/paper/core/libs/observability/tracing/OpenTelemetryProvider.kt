package com.projectcitybuild.pcbridge.paper.core.libs.observability.tracing

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk

class OpenTelemetryProvider {
    /// See https://docs.sentry.io/platforms/java/opentelemetry/setup/agentless/
    private val otel = AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal()
        .addPropertiesSupplier {
            mapOf(
                "otel.logs.exporter" to "none",
                "otel.metrics.exporter" to "none",
                "otel.traces.exporter" to "none"
            )
        }
        .build()

    init {
        logSync.info { "OpenTelemetry initialized" }
    }

    val sdk: OpenTelemetrySdk
        get() = otel.openTelemetrySdk
}
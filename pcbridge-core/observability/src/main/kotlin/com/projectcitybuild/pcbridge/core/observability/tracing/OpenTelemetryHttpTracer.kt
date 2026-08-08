package com.projectcitybuild.pcbridge.core.observability.tracing

import io.opentelemetry.instrumentation.okhttp.v3_0.OkHttpTelemetry
import okhttp3.Call
import okhttp3.OkHttpClient

class OpenTelemetryHttpTracer(
    private val openTelemetry: OpenTelemetryProvider,
) : HttpTracer {
    override fun instrument(client: OkHttpClient): Call.Factory =
        OkHttpTelemetry
            .builder(openTelemetry.sdk)
            .build()
            .newCallFactory(client)
}
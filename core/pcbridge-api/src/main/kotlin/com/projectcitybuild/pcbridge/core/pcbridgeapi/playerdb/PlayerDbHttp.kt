package com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.http.logging.StructuredLoggingInterceptor
import com.projectcitybuild.pcbridge.core.pcbridgeapi.playerdb.services.PlayerDbMinecraftService
import io.opentelemetry.api.OpenTelemetry

class PlayerDbHttp(
    private val baseURL: String = "https://playerdb.co/api/",
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
    private val userAgent: String,
) {
    private val client by lazy {
        PlayerDbClientFactory(
            baseUrl = baseURL,
            logger = logger,
            openTelemetry = openTelemetry,
            userAgent = userAgent,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val minecraft
        get() = PlayerDbMinecraftService(client, responseParser)
}

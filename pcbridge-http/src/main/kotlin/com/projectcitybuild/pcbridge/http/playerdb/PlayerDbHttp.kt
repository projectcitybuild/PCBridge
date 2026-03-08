package com.projectcitybuild.pcbridge.http.playerdb

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.services.BuildHttpService
import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
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

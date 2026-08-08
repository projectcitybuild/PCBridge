package com.projectcitybuild.pcbridge.http.playerdb

import com.projectcitybuild.pcbridge.core.observability.tracing.HttpTracer
import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor

class PlayerDbHttp(
    private val baseURL: String = "https://playerdb.co/api/",
    private val logger: StructuredLoggingInterceptor?,
    private val httpTracer: HttpTracer,
    private val userAgent: String,
) {
    private val client by lazy {
        PlayerDbClientFactory(
            baseUrl = baseURL,
            logger = logger,
            httpTracer = httpTracer,
            userAgent = userAgent,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val minecraft
        get() = PlayerDbMinecraftService(client, responseParser)
}

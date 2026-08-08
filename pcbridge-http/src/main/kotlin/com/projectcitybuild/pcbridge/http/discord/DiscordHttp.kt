package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.core.observability.tracing.HttpTracer
import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor

class DiscordHttp(
    private val logger: StructuredLoggingInterceptor?,
    private val httpTracer: HttpTracer,
) {
    private val client by lazy {
        DiscordClientFactory(
            logger = logger,
            httpTracer = httpTracer,
        ).build()
    }

    val discord
        get() = DiscordHttpService(client)
}

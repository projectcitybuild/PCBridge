package com.projectcitybuild.pcbridge.core.pcbridgeapi.discord

import com.projectcitybuild.pcbridge.core.pcbridgeapi.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.core.http.logging.StructuredLoggingInterceptor
import io.opentelemetry.api.OpenTelemetry

class DiscordHttp(
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
) {
    private val client by lazy {
        DiscordClientFactory(
            logger = logger,
            openTelemetry = openTelemetry,
        ).build()
    }

    val discord
        get() = DiscordHttpService(client)
}

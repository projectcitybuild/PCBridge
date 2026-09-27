package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.http.shared.logging.StructuredLoggingInterceptor
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

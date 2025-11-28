package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import io.opentelemetry.api.OpenTelemetry

class DiscordHttp(
    private val httpLogger: HttpLogger?,
    private val openTelemetry: OpenTelemetry,
) {
    private val client by lazy {
        DiscordClientFactory(
            httpLogger = httpLogger,
            openTelemetry = openTelemetry,
        ).build()
    }

    val discord
        get() = DiscordHttpService(client)
}

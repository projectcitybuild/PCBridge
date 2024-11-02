package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.clients.DiscordClientFactory
import com.projectcitybuild.pcbridge.http.services.discord.DiscordHttpService

class DiscordHttp(
    private val withLogging: Boolean,
) {
    private val client by lazy {
        DiscordClientFactory(
            withLogging = withLogging,
        ).build()
    }

    val discord
        get() = DiscordHttpService(client)
}

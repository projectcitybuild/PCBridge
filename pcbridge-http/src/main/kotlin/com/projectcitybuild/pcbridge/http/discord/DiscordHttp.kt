package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService

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

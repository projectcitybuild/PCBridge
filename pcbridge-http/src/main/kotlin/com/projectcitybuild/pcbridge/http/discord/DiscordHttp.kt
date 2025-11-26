package com.projectcitybuild.pcbridge.http.discord

import com.projectcitybuild.pcbridge.http.discord.services.DiscordHttpService
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger

class DiscordHttp(
    private val httpLogger: HttpLogger?,
) {
    private val client by lazy {
        DiscordClientFactory(
            httpLogger = httpLogger,
        ).build()
    }

    val discord
        get() = DiscordHttpService(client)
}

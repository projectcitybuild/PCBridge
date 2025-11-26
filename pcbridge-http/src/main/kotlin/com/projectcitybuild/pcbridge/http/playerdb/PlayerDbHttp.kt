package com.projectcitybuild.pcbridge.http.playerdb

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.services.BuildHttpService
import com.projectcitybuild.pcbridge.http.playerdb.services.PlayerDbMinecraftService

class PlayerDbHttp(
    private val baseURL: String = "https://playerdb.co/api/",
    private val withLogging: Boolean,
    private val userAgent: String,
) {
    private val client by lazy {
        PlayerDbClientFactory(
            baseUrl = baseURL,
            withLogging = withLogging,
            userAgent = userAgent,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val minecraft
        get() = PlayerDbMinecraftService(client, responseParser)
}

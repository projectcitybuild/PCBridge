package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.clients.MojangClient
import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.mojang.PlayerUUIDHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.TelemetryHttpService

class HttpService(
    private val authToken: String,
    private val baseURL: String,
    private val withLogging: Boolean,
) {
    private val pcbClient by lazy {
        PCBClientFactory(
            authToken = authToken,
            baseUrl = baseURL,
            withLogging = withLogging,
        ).build()
    }

    private val mojangClient by lazy {
        MojangClient(
            withLogging = withLogging,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val playerUuid
        get() = PlayerUUIDHttpService(mojangClient, responseParser)

    val player
        get() = PlayerHttpService(pcbClient, responseParser)

    val telemetry
        get() = TelemetryHttpService(pcbClient, responseParser)

    val register
        get() = RegisterHttpService(pcbClient, responseParser)
}

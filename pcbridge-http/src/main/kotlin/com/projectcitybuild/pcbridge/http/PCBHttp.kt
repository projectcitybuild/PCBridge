package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.pcb.BuildHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.ConfigHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.RegisterHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.TelemetryHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.WarpHttpService

class PCBHttp(
    private val authToken: String,
    private val baseURL: String,
    private val withLogging: Boolean,
) {
    private val client by lazy {
        PCBClientFactory(
            authToken = authToken,
            baseUrl = baseURL,
            withLogging = withLogging,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val builds
        get() = BuildHttpService(client, responseParser)

    val config
        get() = ConfigHttpService(client, responseParser)

    val player
        get() = PlayerHttpService(client, responseParser)

    val register
        get() = RegisterHttpService(client, responseParser)

    val telemetry
        get() = TelemetryHttpService(client, responseParser)

    val warps
        get() = WarpHttpService(client, responseParser)
}

package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.ConfigHttpService
import com.projectcitybuild.pcbridge.http.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.services.PlayerHttpService
import com.projectcitybuild.pcbridge.http.services.TelemetryHttpService
import com.projectcitybuild.pcbridge.http.services.WarpHttpService

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

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val config
        get() = ConfigHttpService(pcbClient, responseParser)

    val player
        get() = PlayerHttpService(pcbClient, responseParser)

    val register
        get() = RegisterHttpService(pcbClient, responseParser)

    val telemetry
        get() = TelemetryHttpService(pcbClient, responseParser)

    val warps
        get() = WarpHttpService(pcbClient, responseParser)
}

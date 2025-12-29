package com.projectcitybuild.pcbridge.http.pcb

import com.projectcitybuild.pcbridge.http.shared.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.pcb.services.BuildHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.ConfigHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.HomeHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.ConnectionHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.UuidBanHttpService
import com.projectcitybuild.pcbridge.http.pcb.services.WarpHttpService
import com.projectcitybuild.pcbridge.http.shared.logging.HttpLogger
import io.opentelemetry.api.OpenTelemetry

class PCBHttp(
    private val authToken: String,
    private val baseURL: String,
    private val httpLogger: HttpLogger?,
    private val openTelemetry: OpenTelemetry,
) {
    private val client by lazy {
        PCBClientFactory(
            authToken = authToken,
            baseUrl = baseURL,
            httpLogger = httpLogger,
            openTelemetry = openTelemetry,
        ).build()
    }

    private val responseParser: ResponseParser
        get() = ResponseParser()

    val builds
        get() = BuildHttpService(client, responseParser)

    val config
        get() = ConfigHttpService(client, responseParser)

    val homes
        get() = HomeHttpService(client, responseParser)

    val connection
        get() = ConnectionHttpService(client, responseParser)

    val register
        get() = RegisterHttpService(client, responseParser)

    val warps
        get() = WarpHttpService(client, responseParser)

    val uuidBans
        get() = UuidBanHttpService(client, responseParser)
}

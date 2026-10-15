package com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb

import com.projectcitybuild.pcbridge.core.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.BuildHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.ConfigHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.HomeHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.RegisterHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.ConnectionHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.OpElevateHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.StatsHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.UuidBanHttpService
import com.projectcitybuild.pcbridge.core.pcbridgeapi.pcb.services.WarpHttpService
import com.projectcitybuild.pcbridge.core.http.logging.StructuredLoggingInterceptor
import io.opentelemetry.api.OpenTelemetry

class PCBHttp(
    private val authToken: String,
    private val baseURL: String,
    private val logger: StructuredLoggingInterceptor?,
    private val openTelemetry: OpenTelemetry,
) {
    private val client by lazy {
        PCBClientFactory(
            authToken = authToken,
            baseUrl = baseURL,
            logger = logger,
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

    val opElevate
        get() = OpElevateHttpService(client, responseParser)

    val register
        get() = RegisterHttpService(client, responseParser)

    val stats
        get() = StatsHttpService(client, responseParser)

    val warps
        get() = WarpHttpService(client, responseParser)

    val uuidBans
        get() = UuidBanHttpService(client, responseParser)
}

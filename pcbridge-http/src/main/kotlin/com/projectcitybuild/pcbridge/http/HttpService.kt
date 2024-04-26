package com.projectcitybuild.pcbridge.http

import com.projectcitybuild.pcbridge.http.clients.MojangClient
import com.projectcitybuild.pcbridge.http.clients.PCBClientFactory
import com.projectcitybuild.pcbridge.http.parsing.ResponseParser
import com.projectcitybuild.pcbridge.http.services.mojang.PlayerUUIDHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.AccountLinkHTTPService
import com.projectcitybuild.pcbridge.http.services.pcb.AggregateHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.IPBanHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerGroupHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.PlayerWarningHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.TelemetryHttpService
import com.projectcitybuild.pcbridge.http.services.pcb.UUIDBanHttpService
import kotlin.coroutines.CoroutineContext

class HttpService(
    private val authToken: String,
    private val baseURL: String,
    private val withLogging: Boolean,
    private val contextBuilder: () -> CoroutineContext,
) {
    private val pcbClient by lazy {
        PCBClientFactory(
            authToken = authToken,
            baseUrl = baseURL,
            withLogging = withLogging
        ).build()
    }

    private val mojangClient by lazy {
        MojangClient(
            withLogging = withLogging,
        ).build()
    }

    private val responseParser: ResponseParser by lazy {
        ResponseParser(contextBuilder)
    }

    val playerUuid
        get() = PlayerUUIDHttpService(mojangClient, responseParser)

    val playerGroup
        get() = PlayerGroupHttpService(pcbClient, responseParser)

    val playerWarning
        get() = PlayerWarningHttpService(pcbClient, responseParser)

    val uuidBan
        get() = UUIDBanHttpService(pcbClient, responseParser)

    val ipBan
        get() = IPBanHttpService(pcbClient, responseParser)

    val aggregate
        get() = AggregateHttpService(pcbClient, responseParser)

    val telemetry
        get() = TelemetryHttpService(pcbClient, responseParser)

    val verificationURL
        get() = AccountLinkHTTPService(pcbClient, responseParser)
}
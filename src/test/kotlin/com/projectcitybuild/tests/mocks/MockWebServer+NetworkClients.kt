package com.projectcitybuild.tests.mocks

import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import okhttp3.mockwebserver.MockWebServer

fun MockWebServer.makeNetworkClients(): NetworkClients {
    val baseUrl = url("")

    return NetworkClients(
            PCBClient(
                    authToken = "",
                    withLogging = false,
                    baseUrl = "$baseUrl/"
            ),
            MojangClient(
                    withLogging = false
            )
    )
}
package com.projectcitybuild.tests.mocks

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient
import okhttp3.mockwebserver.MockWebServer

fun MockWebServer.makeNetworkClients(): APIRequestFactory {
    val baseUrl = url("")

    return APIRequestFactory(
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
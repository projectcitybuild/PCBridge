package com.projectcitybuild.core.infrastructure.network

import com.projectcitybuild.core.infrastructure.network.clients.MojangClient
import com.projectcitybuild.core.infrastructure.network.clients.PCBClient

class APIRequestFactory(
    val pcb: PCBClient,
    val mojang: MojangClient
)

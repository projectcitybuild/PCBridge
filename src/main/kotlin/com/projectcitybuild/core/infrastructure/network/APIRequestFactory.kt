package com.projectcitybuild.core.infrastructure.network

import com.projectcitybuild.core.infrastructure.network.mojang.client.MojangClient
import com.projectcitybuild.core.infrastructure.network.pcb.client.PCBClient

class APIRequestFactory(
    val pcb: PCBClient,
    val mojang: MojangClient
)
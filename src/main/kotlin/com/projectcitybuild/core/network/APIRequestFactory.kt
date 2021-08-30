package com.projectcitybuild.core.network

import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient

class APIRequestFactory(
        val pcb: PCBClient,
        val mojang: MojangClient
)
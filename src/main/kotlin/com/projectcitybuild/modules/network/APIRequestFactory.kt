package com.projectcitybuild.modules.network

import com.projectcitybuild.modules.network.mojang.client.MojangClient
import com.projectcitybuild.modules.network.pcb.client.PCBClient

class APIRequestFactory(
    val pcb: PCBClient,
    val mojang: MojangClient
)
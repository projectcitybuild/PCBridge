package com.projectcitybuild.core.network

import com.projectcitybuild.core.network.mojang.client.MojangClient
import com.projectcitybuild.core.network.pcb.client.PCBClient

class NetworkClients(
        val pcb: PCBClient,
        val mojang: MojangClient
)
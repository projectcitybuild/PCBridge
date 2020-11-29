package com.projectcitybuild.core.api

import com.projectcitybuild.core.api.mojang.client.MojangClient
import com.projectcitybuild.core.api.pcb.client.PCBClient

class APIProvider(
        val pcb: PCBClient,
        val mojang: MojangClient
)
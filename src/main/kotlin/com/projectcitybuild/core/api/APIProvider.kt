package com.projectcitybuild.core.api

import com.projectcitybuild.core.api.client.MojangClient
import com.projectcitybuild.core.api.client.PCBClient

class APIProvider(
        val pcb: PCBClient,
        val mojang: MojangClient
)
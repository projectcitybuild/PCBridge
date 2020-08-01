package com.projectcitybuild.api

import com.projectcitybuild.api.client.MojangClient
import com.projectcitybuild.api.client.PCBClient

class APIProvider(
        val pcb: PCBClient,
        val mojang: MojangClient
)
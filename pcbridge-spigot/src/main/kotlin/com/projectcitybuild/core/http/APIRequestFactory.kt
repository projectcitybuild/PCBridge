package com.projectcitybuild.core.http

import com.projectcitybuild.core.http.clients.MojangClient
import com.projectcitybuild.core.http.clients.PCBClient

class APIRequestFactory(
    val pcb: PCBClient,
    val mojang: MojangClient
)

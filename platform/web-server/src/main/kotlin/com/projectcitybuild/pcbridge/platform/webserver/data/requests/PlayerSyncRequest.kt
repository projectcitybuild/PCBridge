package com.projectcitybuild.pcbridge.platform.webserver.data.requests

import kotlinx.serialization.Serializable

@Serializable
internal data class PlayerSyncRequest(val uuid: String)
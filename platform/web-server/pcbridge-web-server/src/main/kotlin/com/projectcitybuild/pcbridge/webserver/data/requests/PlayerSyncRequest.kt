package com.projectcitybuild.pcbridge.webserver.data.requests

import kotlinx.serialization.Serializable

@Serializable
internal data class PlayerSyncRequest(val uuid: String)
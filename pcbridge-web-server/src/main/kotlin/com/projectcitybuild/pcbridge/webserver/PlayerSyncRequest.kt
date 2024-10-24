package com.projectcitybuild.pcbridge.webserver

import kotlinx.serialization.Serializable

@Serializable
data class PlayerSyncRequest(val uuid: String)
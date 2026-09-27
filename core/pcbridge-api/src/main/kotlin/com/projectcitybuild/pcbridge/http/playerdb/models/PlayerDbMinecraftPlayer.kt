package com.projectcitybuild.pcbridge.http.playerdb.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDbMinecraftPlayer(
    val username: String,
    val id: String,
)
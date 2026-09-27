package com.projectcitybuild.pcbridge.http.playerdb.models

import kotlinx.serialization.Serializable

@Serializable
data class PlayerDbResponse<T>(
    val code: String,
    val message: String,
    val data: PlayerDbResponsePlayer<T>?,
)

@Serializable
data class PlayerDbResponsePlayer<T>(
    val player: T
)
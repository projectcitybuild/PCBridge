package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Authorization(
    @SerializedName("player")
    val player: PlayerData? = null,

    @SerializedName("bans")
    val bans: Bans? = null,
)

@Serializable
data class Bans(
    @SerializedName("uuid")
    val uuid: PlayerBan? = null,

    @SerializedName("ip")
    val ip: IPBan? = null,
)

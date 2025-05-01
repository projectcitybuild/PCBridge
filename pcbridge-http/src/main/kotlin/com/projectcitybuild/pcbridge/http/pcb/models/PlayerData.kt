package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    @SerializedName("account")
    val account: Account? = null,

    @SerializedName("player")
    val player: Player? = null,

    @SerializedName("groups")
    val groups: List<Group> = emptyList(),

    @SerializedName("ban")
    val playerBan: PlayerBan? = null,

    @SerializedName("ip_ban")
    val ipBan: IPBan? = null,

    @SerializedName("badges")
    val badges: List<Badge> = emptyList(),
) {
    val isStaff: Boolean
        get() = groups.firstOrNull { it.groupType?.lowercase() == "staff" } != null
}

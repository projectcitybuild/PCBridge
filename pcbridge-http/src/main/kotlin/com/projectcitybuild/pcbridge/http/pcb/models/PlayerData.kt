package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PlayerData(
    @SerializedName("account")
    val account: Account? = null,

    @SerializedName("player")
    val player: Player? = null,

    @SerializedName("roles")
    val roles: List<Role> = emptyList(),

    @SerializedName("badges")
    val badges: List<Badge> = emptyList(),

    @SerializedName("elevation")
    val elevation: HttpOpElevation? = null,
) {
    val isStaff: Boolean
        get() = roles.firstOrNull { it.roleType?.lowercase() == "staff" } != null
}

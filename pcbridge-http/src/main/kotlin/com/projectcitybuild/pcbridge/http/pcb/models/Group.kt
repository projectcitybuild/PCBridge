package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    @SerializedName("group_id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("minecraft_name")
    val minecraftName: String?,

    @SerializedName("minecraft_hover_text")
    val hoverText: String?,

    @SerializedName("minecraft_display_name")
    val displayName: String?,

    @SerializedName("group_type")
    val groupType: String?,

    @SerializedName("display_priority")
    val displayPriority: Int?,
)

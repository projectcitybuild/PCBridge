package com.projectcitybuild.pcbridge.http.models

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
)

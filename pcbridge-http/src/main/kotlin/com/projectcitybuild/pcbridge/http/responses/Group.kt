package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Group(
    @SerializedName("group_id")
    val id: Int = Math.random().toInt(),
    @SerializedName("name")
    val name: String = "name",
    @SerializedName("alias")
    val alias: String? = null,
    @SerializedName("minecraft_name")
    val minecraftName: String?,
)

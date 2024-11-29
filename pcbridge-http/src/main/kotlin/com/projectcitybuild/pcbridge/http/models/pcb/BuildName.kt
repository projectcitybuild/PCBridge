package com.projectcitybuild.pcbridge.http.models.pcb

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class BuildName(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,
)

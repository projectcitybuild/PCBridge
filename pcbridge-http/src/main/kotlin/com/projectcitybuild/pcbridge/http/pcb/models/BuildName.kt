package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class BuildName(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,
)

package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class HomeLimit(
    @SerializedName("max")
    val max: Int,

    @SerializedName("current")
    val current: Int,

    @SerializedName("sources")
    val sources: Map<String, Int>
)

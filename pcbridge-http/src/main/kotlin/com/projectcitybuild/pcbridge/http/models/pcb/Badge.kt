package com.projectcitybuild.pcbridge.http.models.pcb

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Badge(
    @SerializedName("display_name")
    val displayName: String,

    @SerializedName("unicode_icon")
    val unicodeIcon: String,
)

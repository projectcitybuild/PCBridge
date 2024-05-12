package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName

data class Badge(
    @SerializedName("display_name")
    val displayName: String = "display_name",
    @SerializedName("unicode_icon")
    val unicodeIcon: String = "✦",
)

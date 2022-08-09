package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class Badge(
    @SerializedName("display_name") val displayName: String,
    @SerializedName("unicode_icon") val unicodeIcon: String,
)

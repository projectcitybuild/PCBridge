package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class AuthURL(
    @SerializedName("url") val url: String
)

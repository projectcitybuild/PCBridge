package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class VerificationUrl(
        @SerializedName("url") val url: String
)
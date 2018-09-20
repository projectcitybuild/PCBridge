package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class MojangPlayer(
        @SerializedName("id") val uuid: String,
        @SerializedName("name") val alias: String
)
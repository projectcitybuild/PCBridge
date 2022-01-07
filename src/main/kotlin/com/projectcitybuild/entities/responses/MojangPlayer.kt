package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class MojangPlayer(
        @SerializedName("id") val uuid: String,
        @SerializedName("name") val alias: String
)
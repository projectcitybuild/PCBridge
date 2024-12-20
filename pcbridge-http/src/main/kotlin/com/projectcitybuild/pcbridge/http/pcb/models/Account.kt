package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    @SerializedName("account_id")
    val id: String,

    @SerializedName("username")
    val username: String,
)

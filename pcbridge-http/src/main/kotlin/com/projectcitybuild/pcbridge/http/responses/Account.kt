package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class Account(
    @SerializedName("account_id")
    val id: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("groups")
    val groups: List<Group> = emptyList(),
)

package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Account(
    @SerializedName("account_id")
    val id: String = UUID.randomUUID().toString(),
    @SerializedName("username")
    val username: String = "username",
    @SerializedName("groups")
    val groups: List<Group> = listOf(),
)

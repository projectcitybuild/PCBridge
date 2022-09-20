package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName
import java.util.*

data class Account(
    @SerializedName("account_id") val id: String = UUID.randomUUID().toString(),
    @SerializedName("username") val username: String = "username",
    @SerializedName("groups") val groups: List<Group> = listOf(),
)

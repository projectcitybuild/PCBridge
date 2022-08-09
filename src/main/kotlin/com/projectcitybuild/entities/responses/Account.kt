package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class Account(
    @SerializedName("account_id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("groups") val groups: List<Group>
)

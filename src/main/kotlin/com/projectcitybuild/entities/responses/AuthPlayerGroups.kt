package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class AuthPlayerGroups(
    @SerializedName("account_id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("username") val username: String,
    @SerializedName("groups") val groups: List<Group>
)

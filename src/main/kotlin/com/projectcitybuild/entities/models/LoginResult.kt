package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class LoginResult(
        @SerializedName("id") val id: Int,
        @SerializedName("username") val username: String,
        @SerializedName("email") val email: String,
        @SerializedName("admin") val isAdmin: Boolean,
        @SerializedName("active") val isActive: Boolean?,
        @SerializedName("suspended") val isSuspended: Boolean?
)
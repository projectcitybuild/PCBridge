package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class LoginResult(
        @SerializedName("account_id") val id: Int,
        @SerializedName("email") val email: String,
        @SerializedName("admin") val isAdmin: Boolean,
        @SerializedName("active") val isActive: Boolean?,
        @SerializedName("suspended") val isSuspended: Boolean?
)
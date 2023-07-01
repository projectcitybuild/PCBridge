package com.projectcitybuild.pcbridge.http.responses

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("group_id") val id: Int = Math.random().toInt(),
    @SerializedName("name") val name: String = "name",
    @SerializedName("alias") val alias: String? = null,
    @SerializedName("minecraft_name") val minecraftName: String?,
    @SerializedName("is_default") private val _isDefault: Boolean = false,
    @SerializedName("is_staff") private val _isStaff: Boolean = false,
    @SerializedName("is_admin") private val _isAdmin: Boolean = false
)

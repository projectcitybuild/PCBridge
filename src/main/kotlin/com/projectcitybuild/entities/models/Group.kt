package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class Group(
        @SerializedName("group_id") val id: Int,
        @SerializedName("name") val name: String,
        @SerializedName("alias") val alias: String?,
        @SerializedName("minecraft_name") val minecraftName: String?,
        @SerializedName("is_default") private val _isDefault: Int,
        @SerializedName("is_staff") private val _isStaff: Int,
        @SerializedName("is_admin") private val _isAdmin: Int
) {
    val isDefaultRank: Boolean
        get() = _isDefault == 1

    val isStaffRank: Boolean
        get() = _isStaff == 1

    val isAdminRank: Boolean
        get() = _isAdmin == 1
}
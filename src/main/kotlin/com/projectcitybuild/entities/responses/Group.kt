package com.projectcitybuild.entities.responses

import com.google.gson.annotations.SerializedName

data class Group(
    @SerializedName("group_id") val id: Int = Math.random().toInt(),
    @SerializedName("name") val name: String = "name",
    @SerializedName("alias") val alias: String? = null,
    @SerializedName("minecraft_name") val minecraftName: String?,
    @SerializedName("is_default") private val _isDefault: Int = 0,
    @SerializedName("is_staff") private val _isStaff: Int = 0,
    @SerializedName("is_admin") private val _isAdmin: Int = 0
) {
    val isDefaultRank: Boolean
        get() = _isDefault == 1

    val isStaffRank: Boolean
        get() = _isStaff == 1

    val isAdminRank: Boolean
        get() = _isAdmin == 1
}

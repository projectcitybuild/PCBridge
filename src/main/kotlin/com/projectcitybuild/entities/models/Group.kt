package com.projectcitybuild.entities.models

import com.google.gson.annotations.SerializedName

data class Group(@SerializedName("group_id") val id: Int,
                 @SerializedName("name") val name: String,
                 @SerializedName("alias") val alias: String?,
                 @SerializedName("is_default") val isDefault: Boolean,
                 @SerializedName("is_staff") val isStaff: Boolean,
                 @SerializedName("is_admin") val isAdmin: Boolean)
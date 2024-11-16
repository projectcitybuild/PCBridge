package com.projectcitybuild.pcbridge.http.models.pcb

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedResponse<T>(
    @SerializedName("data")
    val data: T,

    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("to")
    val to: Int,

    @SerializedName("total")
    val total: Int,
)

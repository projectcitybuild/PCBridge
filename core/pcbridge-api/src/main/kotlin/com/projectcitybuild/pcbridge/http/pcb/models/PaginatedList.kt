package com.projectcitybuild.pcbridge.http.pcb.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class PaginatedList<T>(
    @SerializedName("data")
    val data: List<T>,

    @SerializedName("current_page")
    val currentPage: Int,

    @SerializedName("per_page")
    val perPage: Int,

    @SerializedName("to")
    val to: Int,

    @SerializedName("total")
    val total: Int,
)

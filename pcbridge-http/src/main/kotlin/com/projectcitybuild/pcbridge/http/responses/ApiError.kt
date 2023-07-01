package com.projectcitybuild.pcbridge.http.responses

data class ApiError(
    val id: String,
    val title: String,
    val detail: String,
    var status: Int
)

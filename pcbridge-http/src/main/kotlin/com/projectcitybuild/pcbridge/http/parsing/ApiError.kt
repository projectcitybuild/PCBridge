package com.projectcitybuild.pcbridge.http.parsing

data class ApiError(
    val id: String,
    val title: String,
    val detail: String,
    var status: Int
)

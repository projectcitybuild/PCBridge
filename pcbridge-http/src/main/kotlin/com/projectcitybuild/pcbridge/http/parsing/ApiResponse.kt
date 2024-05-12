package com.projectcitybuild.pcbridge.http.parsing

data class ApiResponse<out T>(
    val data: T?,
    val error: ApiError?,
)

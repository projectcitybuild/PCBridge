package com.projectcitybuild.pcbridge.http.responses

data class ApiResponse<out T>(
    val data: T?,
    val error: ApiError?
)

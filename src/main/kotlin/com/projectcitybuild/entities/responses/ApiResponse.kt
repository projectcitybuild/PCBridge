package com.projectcitybuild.entities.responses

data class ApiResponse<out T>(
    val data: T?,
    val error: ApiError?
)
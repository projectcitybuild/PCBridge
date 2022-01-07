package com.projectcitybuild.entities.responses

data class ApiResponse<out Model>(
        val data: Model?,
        val error: ApiError?
)
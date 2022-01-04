package com.projectcitybuild.entities.models

data class ApiResponse<out Model>(
        val data: Model?,
        val error: ApiError?
)
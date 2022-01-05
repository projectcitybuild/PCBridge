package com.projectcitybuild.entities.models;

data class ApiError(
        val id: String,
        val title: String,
        val detail: String,
        var status: Int
): Throwable(message = detail)

package com.projectcitybuild.core.entities.models;

data class ApiError(
        val id: String,
        val title: String,
        val detail: String,

        // FIXME: probably not needed anymore
        var status: Int
)

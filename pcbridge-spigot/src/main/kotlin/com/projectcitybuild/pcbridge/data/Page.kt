package com.projectcitybuild.pcbridge.data

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
)
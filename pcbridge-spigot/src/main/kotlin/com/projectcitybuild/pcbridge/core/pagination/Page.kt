package com.projectcitybuild.pcbridge.core.pagination

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
)

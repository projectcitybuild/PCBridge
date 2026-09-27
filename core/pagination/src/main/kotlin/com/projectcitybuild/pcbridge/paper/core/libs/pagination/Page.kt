package com.projectcitybuild.pcbridge.paper.core.libs.pagination

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
)

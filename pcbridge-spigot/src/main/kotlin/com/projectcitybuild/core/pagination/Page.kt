package com.projectcitybuild.core.pagination

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
)
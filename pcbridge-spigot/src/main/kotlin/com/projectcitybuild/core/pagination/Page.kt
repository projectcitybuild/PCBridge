package com.projectcitybuild.core.pagination

data class Page<T>(
    val data: List<T>,
    val index: Int,
    val totalPages: Int,
)
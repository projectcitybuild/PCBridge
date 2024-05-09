package com.projectcitybuild.data

data class Page<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
)
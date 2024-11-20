package com.projectcitybuild.pcbridge.paper.core.pagination

import kotlin.math.ceil
import kotlin.math.min

class SimplePaginator<T> {
    fun paginate(items: List<T>, pageSize: Int, page: Int): Page<T> {
        check(page >= 1) { "Page must be greater than 0" }

        val startIndex = (page - 1) * pageSize
        val lower = min(items.size, startIndex) // inclusive
        val upper = min(items.size, startIndex + pageSize) // exclusive
        val totalPages = ceil(items.size.toDouble() / pageSize.toDouble()).toInt()

        return Page(
            items = if (lower < upper) items.subList(lower, upper) else emptyList(),
            page = page,
            totalPages = totalPages,
        )
    }
}
package com.projectcitybuild.pcbridge.core.pagination

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SimplePaginatorTest {
    private val paginator = SimplePaginator<String>()

    @Test
    fun `paginate should return requested page and total page count`() {
        val items = listOf("a", "b", "c", "d", "e")

        val page = paginator.paginate(items, pageSize = 2, page = 2)

        assertEquals(
            Page(items = listOf("c", "d"), page = 2, totalPages = 3),
            page
        )
    }

    @Test
    fun `paginate should return final partial page`() {
        val items = listOf("a", "b", "c", "d", "e")

        val page = paginator.paginate(items, pageSize = 2, page = 3)

        assertEquals(
            Page(items = listOf("e"), page = 3, totalPages = 3),
            page
        )
    }

    @Test
    fun `paginate should return empty items for a page beyond the end`() {
        val items = listOf("a", "b", "c")

        val page = paginator.paginate(items, pageSize = 2, page = 4)

        assertEquals(
            Page(items = emptyList<String>(), page = 4, totalPages = 2),
            page
        )
    }

    @Test
    fun `paginate empty list should return zero total pages`() {
        val page = paginator.paginate(emptyList<String>(), pageSize = 2, page = 1)

        assertEquals(
            Page(items = emptyList<String>(), page = 1, totalPages = 0),
            page
        )
    }

    @Test
    fun `paginate should reject page numbers below one`() {
        assertThrows(IllegalStateException::class.java) {
            paginator.paginate(listOf("a"), pageSize = 1, page = 0)
        }
    }
}

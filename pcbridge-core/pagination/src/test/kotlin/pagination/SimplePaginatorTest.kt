package com.projectcitybuild.pcbridge.core.pagination

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SimplePaginatorTest {
    private lateinit var paginator: SimplePaginator<Int>

    @BeforeEach
    fun setUp() {
        paginator = SimplePaginator()
    }

    @Test
    fun `throws exception if page is zero or less`() {
        assertDoesNotThrow {
            paginator.paginate(emptyList(), pageSize = 1, page = 1)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = 1, page = 0)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = 1, page = -1)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = 1, page = -2)
        }
    }

    @Test
    fun `throws exception if page size is zero or less`() {
        assertDoesNotThrow {
            paginator.paginate(emptyList(), pageSize = 1, page = 1)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = 0, page = 1)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = -1, page = 1)
        }
        assertThrows<IllegalStateException> {
            paginator.paginate(emptyList(), pageSize = -2, page = 1)
        }
    }

    @Test
    fun `divides items into pages`() {
        val collection = listOf(1, 2, 3, 4, 5)

        paginator.paginate(collection, pageSize = 2, page = 1).apply {
            assertEquals(3, totalPages)
            assertEquals(1, page)
            assertEquals( listOf(1, 2), items)
        }
        paginator.paginate(collection, pageSize = 2, page = 2).apply {
            assertEquals(3, totalPages)
            assertEquals(2, page)
            assertEquals( listOf(3, 4), items)
        }
        paginator.paginate(collection, pageSize = 2, page = 3).apply {
            assertEquals(3, totalPages)
            assertEquals(3, page)
            assertEquals( listOf(5), items)
        }
    }
}
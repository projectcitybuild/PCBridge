package com.projectcitybuild.pcbridge.webserver.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import kotlin.test.assertEquals

class UuidUtilsTest {
    private val expected = UUID(475826800676128550, -6503483008858150155)
    private val withoutHyphens = "069a79f444e94726a5befca90e38aaf5"
    private val withHyphens = "069a79f4-44e9-4726-a5be-fca90e38aaf5"

    @Test
    fun `accepts non-hyphen uuid`() {
        val uuid = uuidFromAnyString(withoutHyphens)
        assertEquals(expected, uuid)
    }

    @Test
    fun `accepts hyphen uuid`() {
        val uuid = uuidFromAnyString(withHyphens)
        assertEquals(expected, uuid)
    }

    @Test
    fun `throws if invalid input`() {
        assertThrows<Exception> {
            uuidFromAnyString("invalid_string")
        }
    }
}
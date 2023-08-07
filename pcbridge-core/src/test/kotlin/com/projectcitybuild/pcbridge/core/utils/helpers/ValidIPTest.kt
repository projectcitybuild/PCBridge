package com.projectcitybuild.pcbridge.core.utils.helpers

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ValidIPTest {
    @Test
    fun `should return true for valid IP strings`() {
        listOf(
            "192.168.0.1",
            "1.1.1.1",
            "127.0.0.1",
            "255.255.255.255",
        ).forEach { string ->
            assertTrue(
                isValidIP(string)
            )
        }
    }

    @Test
    fun `should return false for invalid IP strings`() {
        listOf(
            "1234.123.123.123",
            "1.1.1",
            "1.1.1.1.",
            ".1.1.1.1",
            "1a.1.1.1",
            "a",
            "a.b.c.d",
        ).forEach { string ->
            assertFalse(
                isValidIP(string)
            )
        }
    }
}
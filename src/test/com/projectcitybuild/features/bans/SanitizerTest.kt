package com.projectcitybuild.features.bans

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SanitizerTest {

    @Test
    fun `sanitize ip should strip slashes and ports`() {
        arrayOf(
            "127.0.0.1",
            "/127.0.0.1",
            "/127.0.0.1:12345",
        ).forEach {
            val sanitized = Sanitizer().sanitizedIP(it)
            assertEquals("127.0.0.1", sanitized)
        }
    }
}

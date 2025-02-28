package com.projectcitybuild.pcbridge.paper.core.support.spigot.utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpigotSanitizerTest {
    @Test
    fun `sanitize ip should strip slashes and ports`() {
        arrayOf(
            "127.0.0.1",
            "/127.0.0.1",
            "/127.0.0.1:12345",
        ).forEach {
            val sanitized = SpigotSanitizer.ipAddress(it)
            assertEquals("127.0.0.1", sanitized)
        }
    }
}

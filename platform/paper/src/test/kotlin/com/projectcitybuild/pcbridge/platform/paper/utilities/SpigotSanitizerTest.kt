package com.projectcitybuild.pcbridge.platform.paper.utilities

import org.junit.jupiter.api.Assertions
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
            Assertions.assertEquals("127.0.0.1", sanitized)
        }
    }
}
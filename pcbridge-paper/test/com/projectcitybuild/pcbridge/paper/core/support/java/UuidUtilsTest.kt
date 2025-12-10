package com.projectcitybuild.pcbridge.paper.core.support.java

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UuidUtilsTest {
    @Test
    fun `creates UUID from string without dashes`() {
        val uuid = uuidFromUnsanitizedString("bee2c0bb2f5b47ce93f9734b3d7fef5f")
        assertEquals(uuid.toString(), "bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f")
    }

    @Test
    fun `creates UUID from string with dashes`() {
        val uuid = uuidFromUnsanitizedString("bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f")
        assertEquals(uuid.toString(), "bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f")
    }

    @Test
    fun `throws on invalid uuid`() {
        assertThrows<IllegalArgumentException> {
            uuidFromUnsanitizedString("invalid-uuid")
        }
    }
}
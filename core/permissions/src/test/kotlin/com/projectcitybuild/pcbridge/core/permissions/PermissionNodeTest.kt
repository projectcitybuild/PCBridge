package com.projectcitybuild.pcbridge.core.permissions

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PermissionNodeTest {
    @Test
    fun `registered permission strings are unique`() {
        val nodes = PermissionNode.entries.map { it.node }
        assertTrue(nodes.size == nodes.toSet().size)
    }

    @Test
    fun `all permission strings are prefixed consistently`() {
        val nodes = PermissionNode.entries.map { it.node }
        assertTrue(nodes.all { it.startsWith("pcbridge.") })
    }
}

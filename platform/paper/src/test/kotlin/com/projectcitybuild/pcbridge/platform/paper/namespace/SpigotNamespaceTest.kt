package com.projectcitybuild.pcbridge.platform.paper.namespace

import org.bukkit.plugin.java.JavaPlugin
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class SpigotNamespaceTest {
    @Test
    fun `get should create a key in plugin namespace`() {
        val plugin = mock<JavaPlugin>()
        whenever(plugin.namespace()).thenReturn("pcbridge")
        val namespace = SpigotNamespace(plugin)
        val key = TestKey("key")

        val namespacedKey = namespace.get(key)

        assertEquals("pcbridge", namespacedKey.namespace)
        assertEquals("key", namespacedKey.key)
    }

    @Test
    fun `get should reuse key for matching identifiers`() {
        val plugin = mock<JavaPlugin>()
        whenever(plugin.namespace()).thenReturn("pcbridge")
        val namespace = SpigotNamespace(plugin)

        assertSame(
            namespace.get(TestKey("key")),
            namespace.get(TestKey("key"))
        )
    }

    private class TestKey(identifier: String) : SpigotNamespace.Key(identifier)
}

package com.projectcitybuild.libs.config

import com.projectcitybuild.libs.config.adapters.MemoryKeyValueStorage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ConfigTest {

    private lateinit var config: Config
    private lateinit var keyValueStorage: MemoryKeyValueStorage

    @BeforeEach
    fun setUp() {
        keyValueStorage = MemoryKeyValueStorage()
        config = Config(keyValueStorage)
    }

    @Test
    fun `get() returns value in storage`() {
        val key = ConfigStorageKey("test", defaultValue = "default")
        keyValueStorage.set(key, "value")

        assertEquals("value", config.get(key))
    }

    @Test
    fun `get() returns default value if not set`() {
        val key = ConfigStorageKey("test", defaultValue = "default")

        assertEquals("default", config.get(key))
    }

    @Test
    fun `get() caches value`() {
        val key = ConfigStorageKey("test", defaultValue = "default")
        keyValueStorage.set(key, "value1")
        assertEquals("value1", config.get(key))

        keyValueStorage.set(key, "value2")
        assertEquals("value1", config.get(key))
    }

    @Test
    fun `set() updates cache and storage`() {
        val key = ConfigStorageKey("test", defaultValue = "default")
        keyValueStorage.set(key, "value1")
        assertEquals("value1", config.get(key))

        keyValueStorage.set(key, "value2")
        assertEquals("value1", config.get(key))

        config.set(key, "value2")
        assertEquals("value2", config.get(key))
        assertEquals("value2", keyValueStorage.get(key))
    }
}

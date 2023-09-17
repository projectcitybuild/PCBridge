package com.projectcitybuild.pcbridge.core.modules.config

import com.projectcitybuild.pcbridge.core.storage.JsonStorage
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever

class ConfigTest {

    @Serializable
    data class TestConfig(
        private val value: String,
    )

    companion object {
        val defaultConfig = TestConfig(value = "default")
    }

    @Mock
    private lateinit var storage: JsonStorage<TestConfig>
    private lateinit var config: Config<TestConfig>

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)

        config = Config(
            default = defaultConfig,
            jsonStorage = storage,
        )
    }

    @Test
    fun `get() returns value from storage on first call`() {
        val expected = TestConfig(value = "uncached")
        whenever(storage.read()).thenReturn(expected)

        assertEquals(expected, config.get())
    }

    @Test
    fun `get() returns cached value on subsequent calls`() {
        val expected = TestConfig(value = "uncached")
        whenever(storage.read()).thenReturn(expected)
        assertEquals(expected, config.get())

        val updated = TestConfig(value = "updated")
        whenever(storage.read()).thenReturn(updated)
        assertEquals(expected, config.get())

        whenever(storage.read()).thenThrow(Error())
        assertEquals(expected, config.get())
    }

    @Test
    fun `get() returns default value if storage returns null`() {
        whenever(storage.read()).thenReturn(null)

        assertEquals(defaultConfig, config.get())
    }

    @Test
    fun `flush() clears the cache`() {
        val expected = TestConfig(value = "uncached")
        whenever(storage.read()).thenReturn(expected)
        assertEquals(expected, config.get())

        val updated = TestConfig(value = "updated")
        whenever(storage.read()).thenReturn(updated)
        assertEquals(expected, config.get())

        config.flush()
        assertEquals(updated, config.get())
    }
}

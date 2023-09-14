package com.projectcitybuild.pcbridge.core.storage.adapters

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.contracts.PlatformLogger
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import java.io.File

class JsonStorageTest {

    @Serializable
    data class TestConfig(
        private val testString: String,
        private val testInt: Int,
        private val testFloat: Float,
        private val testBoolean: Boolean,
        private val testList: List<String>,
        private val testObject: NestedObject,
    )

    @Serializable
    data class NestedObject(
        private val value: String,
    )

    @Serializable
    data class StringConfig(
        private val string: String,
    )

    companion object {
        val testData = TestConfig(
            testString = "string",
            testInt = 123,
            testFloat = 1F,
            testBoolean = true,
            testList = listOf("1", "2", "3"),
            testObject = NestedObject(value = "test")
        )

        val jsonString = "{\"testString\":\"string\",\"testInt\":123,\"testFloat\":1.0,\"testBoolean\":true,\"testList\":[\"1\",\"2\",\"3\"],\"testObject\":{\"value\":\"test\"}}"
    }

    private lateinit var logger: PlatformLogger

    @BeforeEach
    fun setUp() {
        logger = mock(PlatformLogger::class.java)
    }

    @Test
    fun `loads json data from a given file`() {
        val file = File.createTempFile("test_config", ".json")
        file.writeText(jsonString)

        val storage = JsonStorage(
            file = file,
            logger = logger,
            typeToken = object : TypeToken<TestConfig>() {}
        )
        assertEquals(testData, storage.read())
    }

    @Test
    fun `load returns null if error is thrown`() {
        val file = File.createTempFile("test_config", ".json")
        file.delete()

        val storage = JsonStorage(
            file = file,
            logger = logger,
            typeToken = object : TypeToken<TestConfig>() {}
        )
        assertEquals(null, storage.read())
    }

    @Test
    fun `writes json data to a given file`() {
        val file = File.createTempFile("test_config", ".json")
        val storage = JsonStorage(
            file = file,
            logger = logger,
            typeToken = object : TypeToken<TestConfig>() {}
        )
        storage.write(testData)
        assertEquals(jsonString, file.readText())
    }

    @Test
    fun `can read ASCII characters`() {
        val file = File.createTempFile("test_config", ".json")
        file.writeText("{\"string\": \"§6test★\"}")

        val storage = JsonStorage(
            file = file,
            logger = logger,
            typeToken = object : TypeToken<StringConfig>() {}
        )
        val expected = StringConfig(string = "§6test★")
        assertEquals(expected, storage.read())
    }
}

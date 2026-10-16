package com.projectcitybuild.pcbridge.core.filestorage

import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class JsonFileStorageTest {
    @TempDir
    lateinit var tempDirectory: Path

    private val recordStorage = JsonFileStorage(
        TypeToken.get(StoredRecord::class.java)
    )

    @Test
    fun `write sync should create parent directories and serialize using snake case`() {
        val file = tempDirectory.resolve("nested/data.json").toFile()
        val record = StoredRecord(itemName = "warp", totalCount = 3)

        recordStorage.writeSync(file, record)

        assertTrue(file.exists())
        assertTrue(file.readText().contains("\"item_name\":\"warp\""))
        assertTrue(file.readText().contains("\"total_count\":3"))
        assertEquals(record, recordStorage.readSync(file))
    }

    @Test
    fun `read sync should return null when file does not exist`() {
        val file = tempDirectory.resolve("missing.json").toFile()

        assertNull(recordStorage.readSync(file))
    }

    @Test
    fun `read sync should return null for invalid json`() {
        val file = tempDirectory.resolve("invalid.json").toFile()
        file.writeText("{invalid")

        assertNull(recordStorage.readSync(file))
    }

    @Test
    fun `write sync should log and swallow file errors`() {
        val directory = tempDirectory.resolve("directory").toFile()
        assertTrue(directory.mkdir())

        recordStorage.writeSync(directory, StoredRecord())

        assertTrue(directory.isDirectory)
        assertFalse(directory.isFile)
    }

    @Test
    fun `read and write should support generic types asynchronously`() =
        runTest {
            val storage = JsonFileStorage(object : TypeToken<List<StoredRecord>>() {})
            val file = tempDirectory.resolve("records.json").toFile()
            val records = listOf(
                StoredRecord(itemName = "home", totalCount = 1),
                StoredRecord(itemName = "warp", totalCount = 2),
            )
            storage.write(file, records)

            assertEquals(records, storage.read(file))
        }

    private data class StoredRecord(
        val itemName: String = "",
        val totalCount: Int = 0,
    )
}

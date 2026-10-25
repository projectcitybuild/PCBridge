package com.projectcitybuild.pcbridge.runtime.state.store

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.FileStorage
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class StoreTest {
    @TempDir
    lateinit var tempDirectory: File

    @Test
    fun `failed hydration cannot replace existing persisted content`() =
        runTest {
            val file = tempDirectory.resolve("state.json")
            val original = "{invalid-json"
            file.writeText(original)
            val store = store(file)

            assertTrue(runCatching { store.hydrate() }.exceptionOrNull() is RuntimeException)
            assertTrue(runCatching { store.persist() }.exceptionOrNull() is IllegalStateException)

            assertEquals(original, file.readText())
        }

    @Test
    fun `persistence serializes with state mutations`() =
        runTest {
            val writeStarted = CompletableDeferred<Unit>()
            val allowWrite = CompletableDeferred<Unit>()
            var persisted: Map<String, JsonElement>? = null
            val storage = object : FileStorage<Map<String, JsonElement>> {
                override suspend fun read(file: File) = emptyMap<String, JsonElement>()

                override suspend fun write(
                    file: File,
                    data: Map<String, JsonElement>,
                ) {
                    persisted = data
                    writeStarted.complete(Unit)
                    allowWrite.await()
                }

                override fun readSync(file: File) = emptyMap<String, JsonElement>()

                override fun writeSync(file: File, data: Map<String, JsonElement>) {
                    error("Unexpected synchronous write")
                }
            }
            val store = Store(tempDirectory.resolve("state.json"), storage)
            val counter = featureStateKey("counter", 0)
            store.hydrate()
            store.mutate(counter) { 1 }

            val persist = async { store.persist() }
            writeStarted.await()
            val mutation = launch { store.mutate(counter) { 2 } }
            yield()
            assertEquals(1, store.state(counter))

            allowWrite.complete(Unit)
            persist.await()
            mutation.join()

            assertEquals(1, persisted?.get("counter")?.asInt)
            assertEquals(2, store.state(counter))
        }

    private fun store(file: File) = Store(
        file = file,
        fileStorage = JsonFileStorage(
            typeToken = object : TypeToken<Map<String, JsonElement>>() {},
        ),
    )
}

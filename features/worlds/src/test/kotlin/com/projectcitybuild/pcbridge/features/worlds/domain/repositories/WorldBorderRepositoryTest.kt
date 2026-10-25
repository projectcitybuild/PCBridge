package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorderShape
import java.io.File
import java.util.UUID
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.WorldBorder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever

class WorldBorderRepositoryTest {
    @TempDir
    lateinit var temporaryDirectory: File

    @Test
    fun `stores border data in the world's meta folder`() =
        runTest {
            val world = mock(World::class.java)
            val worldId = UUID.randomUUID()
            val server = mock(Server::class.java)
            val worldBorder = mock(WorldBorder::class.java)
            whenever(world.uid).thenReturn(worldId)
            whenever(world.worldFolder).thenReturn(temporaryDirectory)
            whenever(world.worldBorder).thenReturn(worldBorder)
            whenever(server.onlinePlayers).thenReturn(emptyList())
            val border =
                SerializableWorldBorder(
                    centerX = 12.5,
                    centerZ = -8.0,
                    shape = WorldBorderShape.SQUARE,
                    radius = 96.0,
                )
            val repository =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                )

            repository.set(world, border)

            val borderFile = File(temporaryDirectory, "meta/border.json")
            assertTrue(borderFile.exists())
            val loaded =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                ).get(world)
            assertEquals(border, loaded)
        }

    @Test
    fun `rejects border files without an explicit shape`() {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        val borderFile = File(temporaryDirectory, "meta/border.json")
        borderFile.parentFile.mkdirs()
        borderFile.writeText("""{"centerX":0.0,"centerZ":0.0,"radius":10.0}""")
        val repository =
            WorldBorderRepository(
                mock(Server::class.java),
                JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
            )

        assertThrows(IllegalArgumentException::class.java) {
            repository.get(world)
        }
    }

    @Test
    fun `stores rectangular dimensions without applying a restrictive native border`() =
        runTest {
            val world = mock(World::class.java)
            val server = mock(Server::class.java)
            val worldBorder = mock(WorldBorder::class.java)
            whenever(world.uid).thenReturn(UUID.randomUUID())
            whenever(world.worldFolder).thenReturn(temporaryDirectory)
            whenever(world.worldBorder).thenReturn(worldBorder)
            whenever(server.onlinePlayers).thenReturn(emptyList())
            val border =
                SerializableWorldBorder(
                    centerX = 12.5,
                    centerZ = -8.0,
                    shape = WorldBorderShape.RECTANGLE,
                    width = 160.0,
                    depth = 80.0,
                )
            val repository =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                )

            repository.set(world, border)

            val loaded =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                ).load(world)
            assertEquals(border, loaded)
            verify(worldBorder, times(2)).setCenter(0.0, 0.0)
            verify(worldBorder, times(2)).setSize(59_999_968.0)
        }

    @Test
    fun `does not apply a restrictive native border for circles`() =
        runTest {
            val world = mock(World::class.java)
            val server = mock(Server::class.java)
            val worldBorder = mock(WorldBorder::class.java)
            whenever(world.uid).thenReturn(UUID.randomUUID())
            whenever(world.worldFolder).thenReturn(temporaryDirectory)
            whenever(world.worldBorder).thenReturn(worldBorder)
            whenever(server.onlinePlayers).thenReturn(emptyList())
            val repository =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                )

            repository.set(
                world,
                SerializableWorldBorder(
                    centerX = 12.5,
                    centerZ = -8.0,
                    shape = WorldBorderShape.CIRCLE,
                    radius = 96.0,
                ),
            )

            verify(worldBorder).setCenter(0.0, 0.0)
            verify(worldBorder).setSize(59_999_968.0)
        }

    @Test
    fun `clears the configured border and resets the native border`() =
        runTest {
            val world = mock(World::class.java)
            val server = mock(Server::class.java)
            val worldBorder = mock(WorldBorder::class.java)
            whenever(world.uid).thenReturn(UUID.randomUUID())
            whenever(world.worldFolder).thenReturn(temporaryDirectory)
            whenever(world.worldBorder).thenReturn(worldBorder)
            whenever(server.onlinePlayers).thenReturn(emptyList())
            val repository =
                WorldBorderRepository(
                    server,
                    JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
                )
            repository.set(
                world,
                SerializableWorldBorder(
                    centerX = 12.5,
                    centerZ = -8.0,
                    shape = WorldBorderShape.SQUARE,
                    radius = 96.0,
                ),
            )
            val borderFile = File(temporaryDirectory, "meta/border.json")
            assertTrue(borderFile.exists())

            repository.clear(world)

            assertFalse(borderFile.exists())
            assertEquals(null, repository.get(world))
            verify(worldBorder).setCenter(0.0, 0.0)
            verify(worldBorder).setSize(59_999_968.0)
        }
}

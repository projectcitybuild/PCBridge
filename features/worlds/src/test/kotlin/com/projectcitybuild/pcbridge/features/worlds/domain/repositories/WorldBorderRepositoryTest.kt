package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.features.worlds.domain.data.SerializableWorldBorder
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldBorder
import kotlinx.coroutines.test.runTest
import org.bukkit.Server
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.UUID
import org.bukkit.WorldBorder as BukkitWorldBorder

class WorldBorderRepositoryTest {
    @TempDir
    lateinit var temporaryDirectory: File

    @Test
    fun `stores border data in the world's meta folder`() = runTest {
        val world = mock(World::class.java)
        val worldId = UUID.randomUUID()
        val server = mock(Server::class.java)
        val worldBorder = mock(BukkitWorldBorder::class.java)
        whenever(world.uid).thenReturn(worldId)
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        whenever(world.worldBorder).thenReturn(worldBorder)
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val border =
            WorldBorder.Square(
                centerX = 12.5,
                centerZ = -8.0,
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
    fun `caches malformed border files as absent`() {
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

        assertEquals(null, repository.get(world))
        assertEquals(null, repository.get(world))
    }

    @Test
    fun `does not observe external metadata edits until the world cache is evicted`() {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        val borderFile = File(temporaryDirectory, "meta/border.json")
        val repository =
            WorldBorderRepository(
                mock(Server::class.java),
                JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
            )

        assertEquals(null, repository.get(world))
        borderFile.parentFile.mkdirs()
        borderFile.writeText("""{"center_x":4.0,"center_z":-2.0,"shape":"SQUARE","radius":10.0}""")
        assertEquals(null, repository.get(world))

        repository.unload(world)

        assertEquals(WorldBorder.Square(4.0, -2.0, 10.0), repository.get(world))
    }

    @Test
    fun `caches rectangular border files without both dimensions as absent`() {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        val borderFile = File(temporaryDirectory, "meta/border.json")
        borderFile.parentFile.mkdirs()
        borderFile.writeText("""{"centerX":0.0,"centerZ":0.0,"shape":"RECTANGLE","width":16.0}""")
        val repository =
            WorldBorderRepository(
                mock(Server::class.java),
                JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
            )

        assertEquals(null, repository.get(world))
        assertEquals(null, repository.get(world))
    }

    @Test
    fun `evicts a cached border when its world unloads`() {
        val world = mock(World::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        val borderFile = File(temporaryDirectory, "meta/border.json")
        borderFile.parentFile.mkdirs()
        borderFile.writeText("""{"center_x":0.0,"center_z":0.0,"shape":"SQUARE","radius":10.0}""")
        val repository = WorldBorderRepository(
            mock(Server::class.java),
            JsonFileStorage(object : TypeToken<SerializableWorldBorder>() {}),
        )
        assertEquals(WorldBorder.Square(0.0, 0.0, 10.0), repository.get(world))

        borderFile.delete()
        repository.unload(world)

        assertEquals(null, repository.get(world))
    }

    @Test
    fun `stores rectangular dimensions without applying platform behavior`() = runTest {
        val world = mock(World::class.java)
        val server = mock(Server::class.java)
        val worldBorder = mock(BukkitWorldBorder::class.java)
        whenever(world.uid).thenReturn(UUID.randomUUID())
        whenever(world.worldFolder).thenReturn(temporaryDirectory)
        whenever(world.worldBorder).thenReturn(worldBorder)
        whenever(server.onlinePlayers).thenReturn(emptyList())
        val border =
            WorldBorder.Rectangle(
                centerX = 12.5,
                centerZ = -8.0,
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
            ).get(world)
        assertEquals(border, loaded)
        verify(worldBorder, org.mockito.Mockito.never()).setCenter(0.0, 0.0)
    }

    @Test
    fun `persists circle without applying native border`() = runTest {
        val world = mock(World::class.java)
        val server = mock(Server::class.java)
        val worldBorder = mock(BukkitWorldBorder::class.java)
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
            WorldBorder.Circle(
                centerX = 12.5,
                centerZ = -8.0,
                radius = 96.0,
            ),
        )

        verify(worldBorder, org.mockito.Mockito.never()).setCenter(0.0, 0.0)
    }

    @Test
    fun `clears the configured border from storage`() = runTest {
        val world = mock(World::class.java)
        val server = mock(Server::class.java)
        val worldBorder = mock(BukkitWorldBorder::class.java)
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
            WorldBorder.Square(
                centerX = 12.5,
                centerZ = -8.0,
                radius = 96.0,
            ),
        )
        val borderFile = File(temporaryDirectory, "meta/border.json")
        assertTrue(borderFile.exists())

        repository.clear(world)

        assertFalse(borderFile.exists())
        assertEquals(null, repository.get(world))
        verify(worldBorder, org.mockito.Mockito.never()).setCenter(0.0, 0.0)
    }
}

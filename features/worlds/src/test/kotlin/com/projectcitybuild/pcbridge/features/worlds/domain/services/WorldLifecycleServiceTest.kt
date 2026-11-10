package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldDefinitionRepository
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.UnsafeValues
import org.bukkit.World
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.nio.file.Files
import java.nio.file.Path

class WorldLifecycleServiceTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `load refuses to create a replacement for a registered world with no folder`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val (service, server) = serviceFor("missing", worldContainer)

        val failure = assertThrows<IllegalStateException> { service.load("missing") }

        assertTrue(failure.message!!.contains("refusing to create a replacement"))
        verify(server, never()).createWorld(org.mockito.kotlin.any())
    }

    @Test
    fun `load refuses to create a replacement for an empty registered world folder`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        Files.createDirectory(worldContainer.resolve("missing"))
        val (service, server) = serviceFor("missing", worldContainer)

        val failure = assertThrows<IllegalStateException> { service.load("missing") }

        assertTrue(failure.message!!.contains("missing level.dat"))
        verify(server, never()).createWorld(org.mockito.kotlin.any())
    }

    @Test
    fun `regenerate restores the previous folder when the registered environment is invalid`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val worldFolder = Files.createDirectory(worldContainer.resolve("world"))
        Files.writeString(worldFolder.resolve("level.dat"), "old world metadata")
        Files.writeString(worldFolder.resolve("region.mca"), "old world data")
        val service = serviceFor("world", worldContainer, environment = "UNKNOWN").first

        assertThrows<IllegalArgumentException> { service.regenerate("world", confirmed = true) }

        assertEquals("old world metadata", Files.readString(worldFolder.resolve("level.dat")))
        assertEquals("old world data", Files.readString(worldFolder.resolve("region.mca")))
    }

    @Test
    fun `delete restores the world folder when registry removal fails`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val worldFolder = Files.createDirectory(worldContainer.resolve("world"))
        Files.writeString(worldFolder.resolve("level.dat"), "world metadata")
        val definitionRepository = mock(WorldDefinitionRepository::class.java)
        whenever(definitionRepository.unregister("world")).thenReturn(false)
        val (service, _) = serviceFor("world", worldContainer, definitionRepository = definitionRepository)

        val failure = assertThrows<IllegalStateException> {
            service.delete("world", confirmed = true)
        }

        assertTrue(failure.message!!.contains("registration could not be removed"))
        assertEquals("world metadata", Files.readString(worldFolder.resolve("level.dat")))
    }

    @Test
    fun `regenerate backup failure leaves the loaded world untouched`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val worldFolder = Files.createDirectory(worldContainer.resolve("world"))
        Files.writeString(worldFolder.resolve("level.dat"), "world metadata")
        val backupFile = Files.writeString(tempDir.resolve("backup-file"), "not a directory")
        val server = mock(Server::class.java)
        val world = mock(World::class.java)
        whenever(server.isPrimaryThread).thenReturn(true)
        whenever(server.worldContainer).thenReturn(worldContainer.toFile())
        whenever(server.getWorld("world")).thenReturn(world)
        whenever(world.players).thenReturn(emptyList())
        whenever(world.worldFolder).thenReturn(worldFolder.toFile())
        val service = WorldLifecycleService(
            server = server,
            worldDefinitionRepository = mock<WorldDefinitionRepository>().also {
                whenever(it.get("world")).thenReturn(registration("world"))
            },
            worldBorderRepository = mock(),
            backupDirectory = backupFile,
        )

        assertThrows<Exception> { service.regenerate("world", confirmed = true) }

        verify(world).save()
        verify(server, never()).unloadWorld(any<World>(), any<Boolean>())
        assertEquals("world metadata", Files.readString(worldFolder.resolve("level.dat")))
    }

    @Test
    fun `clone removes a partial target after registry persistence fails`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val sourceFolder = Files.createDirectory(worldContainer.resolve("source"))
        Files.writeString(sourceFolder.resolve("level.dat"), "source metadata")
        Files.writeString(sourceFolder.resolve("region.mca"), "source chunks")
        Files.writeString(sourceFolder.resolve("uid.dat"), "source uid")
        val server = mock(Server::class.java)
        val definitionRepository = mock(WorldDefinitionRepository::class.java)
        val createdWorld = mock(World::class.java)
        var targetCreated = false
        whenever(server.isPrimaryThread).thenReturn(true)
        whenever(server.worldContainer).thenReturn(worldContainer.toFile())
        whenever(server.getWorld(any<String>())).thenAnswer { invocation ->
            if (invocation.getArgument<String>(0) == "copy" && targetCreated) createdWorld else null
        }
        whenever(server.createWorld(any())).thenAnswer {
            targetCreated = true
            createdWorld
        }
        whenever(server.unloadWorld(createdWorld, true)).thenAnswer {
            targetCreated = false
            true
        }
        whenever(definitionRepository.get("source")).thenReturn(registration("source"))
        whenever(definitionRepository.get("copy")).thenReturn(null)
        doThrow(IllegalStateException("registry write failed"))
            .whenever(definitionRepository)
            .save(any())
        val service = WorldLifecycleService(
            server = server,
            worldDefinitionRepository = definitionRepository,
            worldBorderRepository = mock(),
            backupDirectory = tempDir.resolve("backups"),
        )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(mock())

            assertThrows<IllegalStateException> {
                service.clone("source", "copy", confirmed = true)
            }
        }

        assertEquals("source metadata", Files.readString(sourceFolder.resolve("level.dat")))
        assertEquals("source chunks", Files.readString(sourceFolder.resolve("region.mca")))
        assertEquals("source uid", Files.readString(sourceFolder.resolve("uid.dat")))
        assertTrue(Files.notExists(worldContainer.resolve("copy")))
    }

    @Test
    fun `clone removes a partial target when copying encounters a symbolic link`() {
        val worldContainer = Files.createDirectory(tempDir.resolve("worlds"))
        val sourceFolder = Files.createDirectory(worldContainer.resolve("source"))
        Files.writeString(sourceFolder.resolve("level.dat"), "source metadata")
        val externalFile = Files.writeString(tempDir.resolve("external-file"), "outside source")
        Files.createSymbolicLink(sourceFolder.resolve("external-link"), externalFile)
        val server = mock(Server::class.java)
        val definitionRepository = mock(WorldDefinitionRepository::class.java)
        whenever(server.isPrimaryThread).thenReturn(true)
        whenever(server.worldContainer).thenReturn(worldContainer.toFile())
        whenever(server.getWorld(any<String>())).thenReturn(null)
        whenever(definitionRepository.get("source")).thenReturn(registration("source"))
        whenever(definitionRepository.get("copy")).thenReturn(null)
        val service = WorldLifecycleService(
            server = server,
            worldDefinitionRepository = definitionRepository,
            worldBorderRepository = mock(),
            backupDirectory = tempDir.resolve("backups"),
        )

        assertThrows<IllegalStateException> {
            service.clone("source", "copy", confirmed = true)
        }

        assertEquals("source metadata", Files.readString(sourceFolder.resolve("level.dat")))
        assertTrue(Files.isSymbolicLink(sourceFolder.resolve("external-link")))
        assertTrue(Files.notExists(worldContainer.resolve("copy")))
    }

    private fun serviceFor(
        name: String,
        worldContainer: Path,
        environment: String = "NORMAL",
        definitionRepository: WorldDefinitionRepository = mock(WorldDefinitionRepository::class.java),
    ): Pair<WorldLifecycleService, Server> {
        val server = mock(Server::class.java)
        whenever(server.isPrimaryThread).thenReturn(true)
        whenever(server.worldContainer).thenReturn(worldContainer.toFile())
        whenever(server.getWorld(name)).thenReturn(null)
        whenever(definitionRepository.get(name)).thenReturn(
            registration(name, environment),
        )
        return WorldLifecycleService(
            server = server,
            worldDefinitionRepository = definitionRepository,
            worldBorderRepository = mock(WorldBorderRepository::class.java),
            backupDirectory = tempDir.resolve("backups"),
        ) to server
    }

    private fun registration(
        name: String,
        environment: String = "NORMAL",
    ) = WorldRegistration(
        definition = WorldDefinition(
            registryKey = name,
            name = name,
            alias = null,
            environment = environment,
            worldType = "NORMAL",
            seed = null,
            generator = null,
            generatorSettings = null,
            generateStructures = null,
            spawnLocation = null,
            scale = null,
            respawnWorld = null,
            pvp = null,
        ),
        isLoaded = false,
    )
}

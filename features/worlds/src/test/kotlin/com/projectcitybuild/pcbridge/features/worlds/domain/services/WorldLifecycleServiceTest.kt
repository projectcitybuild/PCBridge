package com.projectcitybuild.pcbridge.features.worlds.domain.services

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldRegistration
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldBorderRepository
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldDefinitionRepository
import org.bukkit.Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
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

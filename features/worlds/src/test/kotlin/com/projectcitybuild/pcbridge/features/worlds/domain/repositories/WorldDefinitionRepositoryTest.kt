package com.projectcitybuild.pcbridge.features.worlds.domain.repositories

import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.mockito.Mockito.mock
import org.mockito.kotlin.verify
import org.yaml.snakeyaml.LoaderOptions
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import java.nio.file.Files
import java.nio.file.Path
import java.util.logging.Logger

class WorldDefinitionRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `reads Multiverse registry entries without retaining loaded world objects`() {
        val repository = repository(copyFixture())

        val worlds = repository.all()
        val standardWorld = worlds.first { it.definition.registryKey == "minecraft:world" }
        val dottedWorld = worlds.first { it.definition.registryKey == "world.a.b" }

        assertEquals(2, worlds.size)
        assertEquals("world", standardWorld.definition.name)
        assertEquals("Primary", standardWorld.definition.alias)
        assertEquals("NORMAL", standardWorld.definition.environment)
        assertEquals(-5176596003035866649L, standardWorld.definition.seed)
        assertEquals(64.0, standardWorld.definition.spawnLocation!!.y)
        assertTrue(standardWorld.isLoaded)
        assertEquals("world.a.b", dottedWorld.definition.name)
        assertFalse(dottedWorld.isLoaded)
    }

    @Test
    fun `reads and preserves unregistered Multiverse spawn settings`() {
        val file = tempDir.resolve("worlds.yml")
        val original =
            """
            worlds:
              minecraft:world:
                alias: Primary
                read-only:
                  legacy-world-name: world
                spawn-location:
                  ==: MVSpawnSubSettings
                  x: 1.0
                  y: 64.0
                  z: 2.0
                  yaw: 90.0
                  pitch: 0.0
                  spawn:
                    ==: MVSpawnSettings
                    priority: 3
            """.trimIndent()
        Files.writeString(file, original)
        val repository = repository(file)

        val definition = repository.all().single().definition

        assertEquals("world", definition.name)
        assertEquals(64.0, definition.spawnLocation?.y)
        assertEquals(original, Files.readString(file))

        repository.save(definition.copy(alias = "Updated"))

        val savedSpawnLocation = rawYaml(file)
            .nestedMap("worlds")
            .nestedMap("minecraft:world")
            .nestedMap("spawn-location")
        assertEquals("MVSpawnSubSettings", savedSpawnLocation["=="])
        assertEquals("MVSpawnSettings", savedSpawnLocation.nestedMap("spawn")["=="])
        assertEquals(3, savedSpawnLocation.nestedMap("spawn")["priority"])
    }

    @Test
    fun `treats quoted null generator as absent without changing Multiverse configuration`() {
        val file = tempDir.resolve("worlds.yml")
        val original =
            """
            worlds:
              hub:
                generator: 'null'
            """.trimIndent()
        Files.writeString(file, original)
        val repository = repository(file)

        val definition = repository.get("hub")!!.definition

        assertNull(definition.generator)
        assertEquals(original, Files.readString(file))
    }

    @Test
    fun `updates a property while preserving unknown world and root configuration`() {
        val file = copyFixture()
        val repository = repository(file)
        val definition = repository.get("minecraft:world")!!.definition.copy(alias = "Updated alias")

        repository.save(definition)

        val saved = YamlConfiguration.loadConfiguration(file.toFile())
        assertEquals("Updated alias", saved.getString("worlds.minecraft:world.alias"))
        assertEquals("do-not-drop", saved.getString("worlds.minecraft:world.future-property.nested-value"))
        assertTrue(saved.getBoolean("worlds.minecraft:world.spawn-location.futureSpawnProperty.preserve"))
        assertEquals("-5176596003035866649", saved.getString("worlds.minecraft:world.read-only.seed"))
        assertEquals("keep-this", saved.getString("server-note"))
        assertTrue(saved.contains("worlds.world[dot]a[dot]b"))
    }

    @Test
    fun `saves a new world using current Multiverse property keys`() {
        val file = copyFixture()
        val repository = repository(file)
        val definition =
            WorldDefinition(
                registryKey = "world_new",
                name = "world_new",
                alias = "New",
                environment = "NETHER",
                worldType = "AMPLIFIED",
                seed = 41L,
                generator = "example:generator",
                generatorSettings = "{}",
                generateStructures = true,
                spawnLocation = null,
                scale = 8.0,
                respawnWorld = "minecraft:world",
                pvp = false,
            )

        repository.save(definition)

        val saved = YamlConfiguration.loadConfiguration(file.toFile())
        assertEquals("NETHER", saved.getString("worlds.world_new.read-only.environment"))
        assertEquals(41L, saved.getLong("worlds.world_new.read-only.seed"))
        assertEquals("example:generator", saved.getString("worlds.world_new.generator"))
        assertEquals(false, saved.getBoolean("worlds.world_new.pvp"))
        assertEquals("keep-this", saved.getString("server-note"))
    }

    @Test
    fun `unregistering a world only removes its registry entry`() {
        val file = copyFixture()
        val repository = repository(file)

        assertTrue(repository.unregister("world.a.b"))
        assertNull(repository.get("world.a.b"))
        assertFalse(repository.unregister("world.a.b"))
        assertTrue(YamlConfiguration.loadConfiguration(file.toFile()).contains("worlds.minecraft:world"))
    }

    @Test
    fun `does not overwrite malformed world registry data`() {
        val file = tempDir.resolve("worlds.yml")
        Files.writeString(file, "broken: [")
        val repository = repository(file)

        assertThrows<Exception> { repository.all() }
        assertEquals("broken: [", Files.readString(file))
    }

    @Test
    fun `logs and skips an invalid entry while retaining valid registrations`() {
        val file = tempDir.resolve("worlds.yml")
        Files.writeString(
            file,
            """
            worlds:
              valid:
                alias: Valid
              invalid: not-a-mapping
            """.trimIndent(),
        )
        val logger = mock(Logger::class.java)
        val repository = WorldDefinitionRepository(
            worldsFile = file,
            isWorldLoaded = { false },
            logger = logger,
        )

        val registrations = repository.all()

        assertEquals(listOf("valid"), registrations.map { it.definition.name })
        verify(logger).log(
            org.mockito.kotlin.eq(java.util.logging.Level.WARNING),
            org.mockito.kotlin.eq("Skipping invalid world registry entry 'invalid' in $file"),
            org.mockito.kotlin.any<Exception>(),
        )
    }

    private fun repository(file: Path) = WorldDefinitionRepository(
        worldsFile = file,
        isWorldLoaded = { it == "world" },
    )

    private fun copyFixture(): Path {
        val resource = javaClass.getResource("/multiverse-core/worlds.yml")!!.toURI()
        val target = tempDir.resolve("Multiverse-Core/worlds.yml")
        Files.createDirectories(target.parent)
        Files.copy(Path.of(resource), target)
        return target
    }

    private fun rawYaml(file: Path): Map<*, *> = Yaml(SafeConstructor(LoaderOptions())).load<Any?>(Files.readString(file)) as Map<*, *>

    private fun Map<*, *>.nestedMap(key: String): Map<*, *> = get(key) as Map<*, *>
}

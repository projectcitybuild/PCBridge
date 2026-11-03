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
import java.nio.file.Files
import java.nio.file.Path

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
    fun `updates a property while preserving unknown world and root configuration`() {
        val file = copyFixture()
        val repository = repository(file)
        val definition = repository.get("minecraft:world")!!.definition.copy(alias = "Updated alias")

        repository.save(definition)

        val saved = YamlConfiguration.loadConfiguration(file.toFile())
        assertEquals("Updated alias", saved.getString("minecraft:world.alias"))
        assertEquals("do-not-drop", saved.getString("minecraft:world.future-property.nested-value"))
        assertTrue(saved.getBoolean("minecraft:world.spawn-location.futureSpawnProperty.preserve"))
        assertEquals("-5176596003035866649", saved.getString("minecraft:world.read-only.seed"))
        assertEquals("keep-this", saved.getString("server-note"))
        assertTrue(saved.contains("world[dot]a[dot]b"))
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
        assertEquals("NETHER", saved.getString("world_new.read-only.environment"))
        assertEquals(41L, saved.getLong("world_new.read-only.seed"))
        assertEquals("example:generator", saved.getString("world_new.generator"))
        assertEquals(false, saved.getBoolean("world_new.pvp"))
        assertEquals("keep-this", saved.getString("server-note"))
    }

    @Test
    fun `unregistering a world only removes its registry entry`() {
        val file = copyFixture()
        val repository = repository(file)

        assertTrue(repository.unregister("world.a.b"))
        assertNull(repository.get("world.a.b"))
        assertFalse(repository.unregister("world.a.b"))
        assertTrue(YamlConfiguration.loadConfiguration(file.toFile()).contains("minecraft:world"))
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
    fun `does not modify the registry while Multiverse Core is enabled`() {
        val file = copyFixture()
        val original = Files.readString(file)
        val repository =
            WorldDefinitionRepository(
                worldsFile = file,
                isWorldLoaded = { false },
                multiverseCoreEnabled = { true },
            )

        assertThrows<IllegalStateException> {
            repository.save(repository.get("minecraft:world")!!.definition.copy(alias = "Changed"))
        }
        assertEquals(original, Files.readString(file))
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
}

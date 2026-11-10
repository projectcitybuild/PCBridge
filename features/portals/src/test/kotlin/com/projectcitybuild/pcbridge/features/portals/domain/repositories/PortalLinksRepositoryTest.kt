package com.projectcitybuild.pcbridge.features.portals.domain.repositories

import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalLinkType
import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalSettings
import com.projectcitybuild.pcbridge.features.worlds.domain.data.WorldDefinition
import com.projectcitybuild.pcbridge.features.worlds.domain.repositories.WorldDefinitionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class PortalLinksRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `explicit link writes preserve unknown YAML and encode periods in world keys`() {
        val linksFile = tempDir.resolve("links.yml")
        Files.writeString(
            linksFile,
            """
            untouched:
              value: keep
            alpha[dot]world:
              custom-option: retain
            """.trimIndent(),
        )
        val worlds = worldRepository()
        worlds.save(world("alpha.world", "NORMAL"))
        worlds.save(world("beta_world", "NETHER"))
        val repository = repository(linksFile, worlds)

        repository.link("alpha.world", "beta_world", PortalLinkType.NETHER, bidirectional = false)

        val saved = YamlTestConfig.load(linksFile)
        assertEquals("beta_world", saved.getString("alpha[dot]world.nether"))
        assertEquals("retain", saved.getString("alpha[dot]world.custom-option"))
        assertEquals("keep", saved.getString("untouched.value"))
        assertEquals("alpha.world", repository.all().single().sourceWorld)
    }

    @Test
    fun `bidirectional unlink removes both directions and keeps unrelated section values`() {
        val linksFile = tempDir.resolve("links.yml")
        Files.writeString(
            linksFile,
            """
            alpha[dot]world:
              custom-option: retain
            """.trimIndent(),
        )
        val worlds = worldRepository()
        worlds.save(world("alpha.world", "NORMAL"))
        worlds.save(world("beta_world", "THE_END"))
        val repository = repository(linksFile, worlds)
        repository.link("alpha.world", "beta_world", PortalLinkType.END, bidirectional = true)

        assertTrue(repository.unlink("alpha.world", PortalLinkType.END, bidirectional = true))
        assertNull(repository.get("alpha.world", PortalLinkType.END))
        assertNull(repository.get("beta_world", PortalLinkType.END))
        assertEquals("retain", YamlTestConfig.load(linksFile).getString("alpha[dot]world.custom-option"))
        assertFalse(repository.unlink("alpha.world", PortalLinkType.END, bidirectional = false))
    }

    @Test
    fun `bidirectional unlink does not remove a destination link that points elsewhere`() {
        val worlds = worldRepository()
        listOf("alpha.world", "beta_world", "gamma_world").forEach { name ->
            worlds.save(world(name, "NORMAL"))
        }
        val repository = repository(tempDir.resolve("links.yml"), worlds)
        repository.link("alpha.world", "beta_world", PortalLinkType.NETHER, bidirectional = false)
        repository.link("beta_world", "gamma_world", PortalLinkType.NETHER, bidirectional = false)

        assertTrue(repository.unlink("alpha.world", PortalLinkType.NETHER, bidirectional = true))

        assertEquals("gamma_world", repository.get("beta_world", PortalLinkType.NETHER))
    }

    @Test
    fun `automatic names resolve in both directions only for registered worlds`() {
        val worlds = worldRepository()
        worlds.save(world("city", "NORMAL"))
        worlds.save(world("city_nether", "NETHER"))
        val repository = repository(tempDir.resolve("links.yml"), worlds)

        assertEquals("city_nether", repository.resolve("city", "NORMAL", PortalLinkType.NETHER)?.worldName)
        assertEquals("city", repository.resolve("city_nether", "NETHER", PortalLinkType.NETHER)?.worldName)
        assertNull(repository.resolve("unknown", "NORMAL", PortalLinkType.NETHER))
    }

    @Test
    fun `portal configuration reads existing options without rewriting them`() {
        val configFile = tempDir.resolve("config.yml")
        val original =
            """
            portal-auto-link-when:
              nether:
                prefix: "n_"
                suffix: "_hell"
              end:
                prefix: ""
                suffix: "_void"
            bounceback: false
            teleport-entities: false
            unknown-option: keep
            """.trimIndent()
        Files.writeString(configFile, original)

        val settings = PortalSettings.load(configFile)

        assertEquals("n_", settings.netherPrefix)
        assertEquals("_hell", settings.netherSuffix)
        assertEquals("_void", settings.endSuffix)
        assertFalse(settings.bounceBack)
        assertFalse(settings.teleportEntities)
        assertEquals(original, Files.readString(configFile))
    }

    @Test
    fun `legacy portal links are read and can be disabled without rewriting config`() {
        val linksFile = tempDir.resolve("links.yml")
        val configFile = tempDir.resolve("config.yml")
        val original =
            """
            worlds:
              alpha:
                portalgoesto:
                  NETHER: beta_nether
                  ENDER: beta_end
            """.trimIndent()
        Files.writeString(configFile, original)
        val repository =
            PortalLinksRepository(
                linksFile = linksFile,
                legacyConfigFile = configFile,
                settings = PortalSettings(),
                worldDefinitionRepository = worldRepository(),
            )

        assertEquals("beta_nether", repository.get("alpha", PortalLinkType.NETHER))
        assertEquals(2, repository.all().size)
        assertTrue(repository.unlink("alpha", PortalLinkType.NETHER, bidirectional = false))

        assertEquals("alpha", repository.get("alpha", PortalLinkType.NETHER))
        assertEquals("beta_end", repository.get("alpha", PortalLinkType.END))
        assertEquals(original, Files.readString(configFile))
        assertEquals("alpha", YamlTestConfig.load(linksFile).getString("alpha.nether"))
    }

    private fun repository(
        linksFile: Path,
        worlds: WorldDefinitionRepository = worldRepository(),
    ) = PortalLinksRepository(
        linksFile = linksFile,
        settings = PortalSettings(),
        worldDefinitionRepository = worlds,
    )

    private fun worldRepository() = WorldDefinitionRepository(
        worldsFile = tempDir.resolve("worlds.yml"),
        isWorldLoaded = { false },
    )

    private fun world(
        name: String,
        environment: String,
    ) = WorldDefinition(
        registryKey = name,
        name = name,
        alias = null,
        environment = environment,
        worldType = null,
        seed = null,
        generator = null,
        generatorSettings = null,
        generateStructures = null,
        spawnLocation = null,
        scale = null,
        respawnWorld = null,
        pvp = null,
    )

    private object YamlTestConfig {
        fun load(path: Path): org.bukkit.configuration.file.YamlConfiguration = org.bukkit.configuration.file.YamlConfiguration().apply {
            load(path.toFile())
        }
    }
}

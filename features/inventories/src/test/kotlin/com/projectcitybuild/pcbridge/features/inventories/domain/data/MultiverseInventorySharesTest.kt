package com.projectcitybuild.pcbridge.features.inventories.domain.data

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class MultiverseInventorySharesTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `inventory and stats names expand to Multiverse share sets`() {
        assertEquals(
            setOf("inventory_contents", "armor_contents", "ender_chest", "off_hand"),
            MultiverseInventoryShares.expand("inventory"),
        )
        assertTrue(MultiverseInventoryShares.expand("stats").containsAll(setOf("hit_points", "game_statistics", "advancements")))
        assertEquals(setOf("ender_chest"), MultiverseInventoryShares.expand("ender-chest"))
    }

    @Test
    fun `world groups select configured shares and remove disabled shares`() {
        val group =
            InventoryWorldGroup(
                name = "survival",
                worlds = setOf("world", "world_nether"),
                shares = setOf("inventory", "stats"),
                disabledShares = setOf("health"),
                spawnWorld = null,
                spawnPriority = null,
            )

        val resolved =
            MultiverseInventoryShares.resolveGroups(
                config =
                    MultiverseInventoryConfig(
                        defaultUngroupedWorlds = false,
                        activeOptionalShares = setOf("stats"),
                    ),
                groups = listOf(group),
                worldName = "world_nether",
            ).single()

        assertTrue(resolved.shares.containsAll(setOf("inventory_contents", "xp", "game_statistics")))
        assertTrue(resolved.shares.none { it in setOf("hit_points", "max_hit_points", "remaining_air") })
        assertTrue(resolved.disabledShares.containsAll(setOf("hit_points", "max_hit_points", "remaining_air")))
    }

    @Test
    fun `ungrouped worlds use the default Multiverse group only when configured`() {
        val groups =
            MultiverseInventoryShares.resolveGroups(
                config = MultiverseInventoryConfig(defaultUngroupedWorlds = true),
                groups = emptyList(),
                worldName = "event world",
            )

        assertEquals("default", groups.single().name)
        assertTrue(groups.single().shares.containsAll(MultiverseInventoryShares.standard))
    }

    @Test
    fun `unknown configured share fails instead of being silently ignored`() {
        assertThrows(IllegalArgumentException::class.java) {
            MultiverseInventoryShares.expand("custom-unrecognized-share")
        }
    }

    @Test
    fun `inventory configuration reads last location and bed validation from sharables`() {
        val configFile = tempDir.resolve("config.yml")
        Files.writeString(
            configFile,
            """
            sharables:
              apply-last-location-for-all-teleports: false
              validate-bed-anchor-respawn-location: false
            """.trimIndent(),
        )

        val config = MultiverseInventoryConfig.load(configFile)

        assertFalse(config.applyLastLocationForAllTeleports)
        assertFalse(config.validateBedAnchorRespawnLocation)
        assertEquals(true, MultiverseInventoryConfig().applyLastLocationForAllTeleports)
        assertEquals(true, MultiverseInventoryConfig().validateBedAnchorRespawnLocation)
    }
}

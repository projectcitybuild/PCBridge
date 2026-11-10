package com.projectcitybuild.pcbridge.features.portals.domain.data

import org.bukkit.Material
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class MultiversePortalsSettingsTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `reads custom portal options without modifying the existing config`() {
        val configFile = tempDir.resolve("config.yml")
        val original =
            """
            portal-usage:
              enforce-portal-access: false
              send-no-permission-messages: false
              portals-default-to-nether: true
              use-on-move: false
              portal-cooldown: 2500
            portal-creation:
              wand-material: GOLDEN_AXE
              clear-on-remove: true
              frame-materials:
                - OBSIDIAN
            unknown-option: keep
            """.trimIndent()
        Files.writeString(configFile, original)

        val settings = MultiversePortalsSettings.load(configFile)

        assertFalse(settings.enforcePortalAccess)
        assertFalse(settings.sendNoPermissionMessages)
        assertTrue(settings.portalsDefaultToNether)
        assertFalse(settings.useOnMove)
        assertEquals(2_500, settings.portalCooldownMillis)
        assertEquals(Material.GOLDEN_AXE, settings.wandMaterial)
        assertTrue(settings.clearOnRemove)
        assertEquals(setOf(Material.OBSIDIAN), settings.frameMaterials)
        assertEquals(original, Files.readString(configFile))
    }

    @Test
    fun `uses safe defaults when the config file is missing`() {
        val settings = MultiversePortalsSettings.load(tempDir.resolve("missing.yml"))

        assertTrue(settings.enforcePortalAccess)
        assertTrue(settings.sendNoPermissionMessages)
        assertFalse(settings.portalsDefaultToNether)
        assertTrue(settings.useOnMove)
        assertEquals(1_000, settings.portalCooldownMillis)
        assertEquals(Material.WOODEN_PICKAXE, settings.wandMaterial)
        assertFalse(settings.clearOnRemove)
        assertTrue(settings.frameMaterials.isEmpty())
    }
}

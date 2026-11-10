package com.projectcitybuild.pcbridge.features.portals.domain.repositories

import com.projectcitybuild.pcbridge.features.portals.domain.data.PortalRegion
import org.bukkit.configuration.file.YamlConfiguration
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class CustomPortalsRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `reads current and legacy portal locations without rewriting source file`() {
        val portalsFile = tempDir.resolve("portals.yml")
        val original =
            """
            untouched:
              value: keep
            portals:
              lobby:
                location: "world:1,2,3:4,5,6"
                action:
                  type: multiverse-destination
                  value: "w:world"
                custom-option: retain
              legacy:
                world: world
                location: "7,8,9:10,11,12"
                destination: "e:world:1,64,2"
            """.trimIndent()
        Files.writeString(portalsFile, original)
        val repository = CustomPortalsRepository(portalsFile)

        val portals = repository.all().associateBy { it.name }

        assertEquals(2, portals.size)
        assertEquals(PortalRegion("world", 1, 2, 3, 4, 5, 6), portals["lobby"]?.region)
        assertEquals("w:world", portals["lobby"]?.destination)
        assertEquals(PortalRegion("world", 7, 8, 9, 10, 11, 12), portals["legacy"]?.region)
        assertEquals("e:world:1,64,2", portals["legacy"]?.destination)
        assertEquals(original, Files.readString(portalsFile))
    }

    @Test
    fun `mutations preserve unrelated values and the original portal structure`() {
        val portalsFile = tempDir.resolve("portals.yml")
        Files.writeString(
            portalsFile,
            """
            unrelated:
              value: keep
            portals:
              existing:
                location: "world:0,1,2:2,3,4"
                custom-option: retain
            """.trimIndent(),
        )
        val repository = CustomPortalsRepository(portalsFile)

        repository.create(
            name = "new_portal",
            region = PortalRegion("world", 1, 2, 3, 4, 5, 6),
            destination = "w:world",
            owner = "admin",
        )
        repository.setDestination("new_portal", "p:existing")

        val saved = YamlConfiguration().apply { load(portalsFile.toFile()) }
        assertEquals("keep", saved.getString("unrelated.value"))
        assertEquals("retain", saved.getString("portals.existing.custom-option"))
        assertEquals("world:1,2,3:4,5,6", saved.getString("portals.new_portal.location"))
        assertEquals("p:existing", saved.getString("portals.new_portal.action.value"))

        assertTrue(repository.remove("new_portal"))
        assertFalse(repository.remove("new_portal"))
        assertNull(YamlConfiguration().apply { load(portalsFile.toFile()) }.get("portals.new_portal"))
    }

    @Test
    fun `rejects unsafe names before using them as YAML paths`() {
        val repository = CustomPortalsRepository(tempDir.resolve("portals.yml"))

        assertThrows(IllegalArgumentException::class.java) {
            repository.setDestination("bad.name", "w:world")
        }
        assertThrows(IllegalArgumentException::class.java) {
            repository.remove("../portal")
        }
    }
}

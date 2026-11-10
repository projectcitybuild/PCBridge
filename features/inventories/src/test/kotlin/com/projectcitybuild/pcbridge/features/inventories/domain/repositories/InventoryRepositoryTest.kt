package com.projectcitybuild.pcbridge.features.inventories.domain.repositories

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileLocation
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import java.util.concurrent.Executors

class InventoryRepositoryTest {
    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `validation reads Multiverse hierarchy and reports existing profiles`() {
        val dataDirectory = copyFixture()
        val repository = repository(dataDirectory)

        val report = repository.inspect()
        val groupProfile =
            repository.readProfile(
                InventoryProfileLocation("groups", "survival", "Alice"),
            )
        val worldProfile =
            repository.readProfile(
                InventoryProfileLocation("worlds", "world", "Alice"),
            )
        val globalProfile = repository.readGlobalProfile(UUID.fromString(PLAYER_ID))
        val nameMapping = repository.readPlayerNames()

        assertTrue(report.groupConfigurationPresent)
        assertTrue(report.inventoryConfigurationPresent)
        assertTrue(report.playerNameMapPresent)
        assertEquals(2, report.worldProfiles)
        assertEquals(1, report.groupProfiles)
        assertEquals(1, report.globalProfiles)
        assertTrue(report.malformedJsonFiles.isEmpty())
        assertEquals(
            "AAECAwQ=",
            groupProfile!!
                .getAsJsonObject("SURVIVAL")
                .getAsJsonObject("inventoryContents")
                .get("0")
                .asString,
        )
        assertEquals(
            "COMPASS",
            worldProfile!!
                .getAsJsonObject("ADVENTURE")
                .getAsJsonObject("inventoryContents")
                .getAsJsonObject("1")
                .get("type")
                .asString,
        )
        assertEquals("event_world", globalProfile!!.getAsJsonObject("playerData").get("lastWorld").asString)
        assertEquals("FormerName", nameMapping!!.get("123e4567-e89b-12d3-a456-426614174001").asString)
    }

    @Test
    fun `updates are active by default and preserve unknown fields profile types and Bukkit item representation`() {
        val dataDirectory = copyFixture()
        val repository = repository(dataDirectory)
        val location = InventoryProfileLocation("groups", "survival", "Alice")

        repository.updateProfile(
            location,
            InventoryProfileType.SURVIVAL,
            mapOf("inventoryContents" to JsonObject().apply { add("1", JsonPrimitive("new-byte-data")) }),
        )

        val saved = JsonParser.parseString(Files.readString(dataDirectory.resolve("groups/survival/Alice.json"))).asJsonObject
        val survival = saved.getAsJsonObject("SURVIVAL")

        assertEquals("new-byte-data", survival.getAsJsonObject("inventoryContents").get("1").asString)
        assertTrue(survival.getAsJsonObject("armorContents").getAsJsonObject("0").has("=="))
        assertEquals(17, survival.getAsJsonObject("stats").get("pcbridge_future_stat").asInt)
        assertTrue(survival.getAsJsonObject("futureShare").get("kept").asBoolean)
        assertEquals(
            "AQID",
            saved
                .getAsJsonObject("CREATIVE")
                .getAsJsonObject("inventoryContents")
                .get("0")
                .asString,
        )
        assertEquals("yes", saved.getAsJsonObject("futureProfileType").get("retained").asString)
        assertTrue(
            Files.isRegularFile(
                Files.list(tempDir.resolve("backups")).use {
                    it.findFirst().orElseThrow().resolve("groups/survival/Alice.json")
                },
            ),
        )
    }

    @Test
    fun `nested stats updates preserve unknown stat values`() {
        val dataDirectory = copyFixture()
        val repository = repository(dataDirectory)

        repository.updateProfile(
            InventoryProfileLocation("groups", "survival", "Alice"),
            InventoryProfileType.SURVIVAL,
            fields = emptyMap(),
            statsFields = mapOf("hp" to JsonPrimitive(15.0)),
        )

        val profile =
            JsonParser
                .parseString(
                    Files.readString(dataDirectory.resolve("groups/survival/Alice.json")),
                ).asJsonObject
                .getAsJsonObject("SURVIVAL")
                .getAsJsonObject("stats")
        assertEquals(15.0, profile.get("hp").asDouble)
        assertEquals(9, profile.get("minecraft:custom").asInt)
        assertEquals(17, profile.get("pcbridge_future_stat").asInt)
    }

    @Test
    fun `active writes fail when a complete backup cannot copy a symbolic link`() {
        val dataDirectory = copyFixture()
        val linkedFile = tempDir.resolve("linked-player-data.json")
        Files.writeString(linkedFile, "{}")
        Files.createSymbolicLink(dataDirectory.resolve("external.json"), linkedFile)
        val repository = repository(dataDirectory)

        assertThrows(IllegalStateException::class.java) {
            repository.updateGlobalPlayerData(
                UUID.fromString(PLAYER_ID),
                mapOf("lastWorld" to JsonPrimitive("world")),
            )
        }

        Files.list(tempDir.resolve("backups")).use { backups -> assertEquals(0, backups.count()) }
        assertTrue(Files.exists(dataDirectory.resolve("groups/survival/Alice.json")))
    }

    @Test
    fun `global metadata updates retain unrelated fields`() {
        val dataDirectory = copyFixture()
        val repository = repository(dataDirectory)

        repository.updateGlobalPlayerData(
            UUID.fromString(PLAYER_ID),
            mapOf("lastWorld" to JsonPrimitive("world")),
        )

        val saved =
            JsonParser
                .parseString(
                    Files.readString(dataDirectory.resolve("players/$PLAYER_ID.json")),
                ).asJsonObject
        assertEquals("world", saved.getAsJsonObject("playerData").get("lastWorld").asString)
        assertEquals(
            1,
            saved
                .getAsJsonObject("playerData")
                .getAsJsonObject("futureGlobalValue")
                .get("preserve")
                .asInt,
        )
        assertEquals("keep", saved.get("futureMetadata").asString)
    }

    @Test
    fun `concurrent profile updates are serialized without losing independent sections`() {
        val dataDirectory = copyFixture()
        val repository = repository(dataDirectory)
        val workers = Executors.newFixedThreadPool(4)

        try {
            val updates =
                (0 until 16).map { index ->
                    workers.submit {
                        repository.updateProfile(
                            InventoryProfileLocation("groups", "survival", "Alice"),
                            if (index % 2 == 0) InventoryProfileType.SURVIVAL else InventoryProfileType.CREATIVE,
                            mapOf("concurrent-$index" to JsonPrimitive(index)),
                        )
                    }
                }
            updates.forEach { it.get() }
        } finally {
            workers.shutdownNow()
        }

        val profile =
            JsonParser
                .parseString(
                    Files.readString(dataDirectory.resolve("groups/survival/Alice.json")),
                ).asJsonObject
        assertEquals(8, profile.getAsJsonObject("SURVIVAL").entrySet().count { it.key.startsWith("concurrent-") })
        assertEquals(8, profile.getAsJsonObject("CREATIVE").entrySet().count { it.key.startsWith("concurrent-") })
    }

    @Test
    fun `inspection reports malformed profile data without rewriting it`() {
        val dataDirectory = copyFixture()
        val malformed = dataDirectory.resolve("worlds/world/broken.json")
        Files.writeString(malformed, "{broken")
        val repository = repository(dataDirectory)

        val report = repository.inspect()

        assertEquals(listOf(malformed.toFile()), report.malformedJsonFiles)
        assertEquals("{broken", Files.readString(malformed))
    }

    @Test
    fun `inspection reports malformed YAML without changing the source`() {
        val dataDirectory = copyFixture()
        val malformed = dataDirectory.resolve("groups.yml")
        Files.writeString(malformed, "groups: [")
        val repository = repository(dataDirectory)

        val report = repository.inspect()

        assertEquals(listOf(malformed.toFile()), report.malformedYamlFiles)
        assertEquals("groups: [", Files.readString(malformed))
    }

    @Test
    fun `group configuration is readable and share conflicts are reported without rewriting it`() {
        val dataDirectory = copyFixture()
        val groupsFile = dataDirectory.resolve("groups.yml")
        val updatedGroups =
            Files.readString(groupsFile) +
                """

                  overlapping:
                    worlds:
                      - world
                    shares:
                      - inventory
                """.trimIndent().prependIndent("  ")
        Files.writeString(groupsFile, updatedGroups)
        val originalGroups = Files.readString(groupsFile)
        val repository = repository(dataDirectory)

        val groups = repository.readWorldGroups()
        val conflicts = repository.findGroupShareConflicts()

        assertEquals(setOf("world", "world_nether"), groups.first { it.name == "survival" }.worlds)
        assertEquals(setOf("inventory", "armor", "offhand", "stats"), groups.first { it.name == "survival" }.shares)
        assertEquals(1, conflicts.size)
        assertEquals("world", conflicts.single().world)
        assertEquals("inventory", conflicts.single().share)
        assertEquals(setOf("survival", "overlapping"), conflicts.single().groups)
        assertEquals(originalGroups, Files.readString(groupsFile))
    }

    @Test
    fun `existing Multiverse settings accept current nested and legacy flat paths`() {
        val repository = repository(copyFixture())

        val configuration = repository.readConfiguration()

        assertTrue(configuration.defaultUngroupedWorlds)
        assertTrue(configuration.useByteSerializationForInventoryData)
        assertEquals(setOf("health", "stats"), configuration.activeOptionalShares)
        assertEquals(41, configuration.inventorySize)
    }

    @Test
    fun `profile name lookup keeps using the Multiverse mapped filename`() {
        val repository = repository(copyFixture())

        assertEquals("FormerName", repository.profileName(UUID.fromString("123e4567-e89b-12d3-a456-426614174001"), "Alice"))
        assertEquals("New Player", repository.profileName(UUID.randomUUID(), "New Player"))
    }

    private fun repository(dataDirectory: Path) = InventoryRepository(
        dataDirectory = dataDirectory,
        backupDirectory = tempDir.resolve("backups"),
    )

    private fun copyFixture(): Path {
        val source = javaClass.getResource("/multiverse-inventories")!!.toURI()
        val fixture = Path.of(source)
        val destination = tempDir.resolve("Multiverse-Inventories")
        Files.walk(fixture).use { paths ->
            paths.forEach { path ->
                val target = destination.resolve(fixture.relativize(path).toString())
                if (Files.isDirectory(path)) {
                    Files.createDirectories(target)
                } else {
                    Files.createDirectories(target.parent)
                    Files.copy(path, target)
                }
            }
        }
        return destination
    }

    private companion object {
        const val PLAYER_ID = "123e4567-e89b-12d3-a456-426614174000"
    }
}

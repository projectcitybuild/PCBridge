package com.projectcitybuild.pcbridge.features.inventories.domain.services

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonPrimitive
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileLocation
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileReadResult
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryProfileType
import com.projectcitybuild.pcbridge.features.inventories.domain.data.InventoryWorldGroup
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryConfig
import com.projectcitybuild.pcbridge.features.inventories.domain.data.MultiverseInventoryShares
import com.projectcitybuild.pcbridge.features.inventories.domain.repositories.InventoryRepository
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.UnsafeValues
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Base64
import java.util.UUID

class InventoryTransitionServiceTest {
    private val service =
        InventoryTransitionService(
            repository = mock<InventoryRepository>(),
            config = MultiverseInventoryConfig(),
        )

    @Test
    fun `decodes Paper schema item maps without a Bukkit serialization marker`() {
        val unsafe = mock<UnsafeValues>()
        val expected = mock<ItemStack>()
        whenever(unsafe.deserializeStack(any())).thenReturn(expected)

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(unsafe)

            val decoded =
                service.decodeItem(
                    JsonParser.parseString(
                        """{"DataVersion":4556,"id":"minecraft:diamond","count":1,"schema_version":1}""",
                    ),
                )

            assertSame(expected, decoded)
        }

        verify(unsafe).deserializeStack(any())
    }

    @Test
    fun `decodes legacy Bukkit item maps`() {
        val expected = mock<ItemStack>()

        mockStatic(ItemStack::class.java).use { itemStacks ->
            itemStacks.`when`<ItemStack> { ItemStack.deserialize(any()) }.thenReturn(expected)

            val decoded =
                service.decodeItem(
                    JsonParser.parseString(
                        """{"==":"org.bukkit.inventory.ItemStack","v":4189,"type":"SEA_LANTERN","amount":2}""",
                    ),
                )

            assertSame(expected, decoded)
            itemStacks.verify { ItemStack.deserialize(any()) }
        }
    }

    @Test
    fun `decodes byte-serialized item data`() {
        val unsafe = mock<UnsafeValues>()
        val expected = mock<ItemStack>()
        whenever(unsafe.deserializeItem(any())).thenReturn(expected)
        val serialized = Base64.getEncoder().encodeToString(byteArrayOf(1, 2, 3))

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(unsafe)

            assertSame(expected, service.decodeItem(JsonPrimitive(serialized)))
        }

        verify(unsafe).deserializeItem(any())
    }

    @Test
    fun `decodes Multiverse fixture slots without shifting slot forty`() {
        val unsafe = mock<UnsafeValues>()
        val firstSlot = mock<ItemStack>()
        val fortiethSlot = mock<ItemStack>()
        whenever(unsafe.deserializeItem(any())).thenReturn(firstSlot, fortiethSlot)
        val fixture = javaClass.getResource("/multiverse-inventories/groups/survival/Alice.json")!!
        val profile = JsonParser.parseString(fixture.readText())
            .asJsonObject
            .getAsJsonObject("SURVIVAL")
            .get("inventoryContents")

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<UnsafeValues> { Bukkit.getUnsafe() }.thenReturn(unsafe)

            val contents = service.decodeItems(profile, size = 41)

            assertSame(firstSlot, contents[0])
            assertNull(contents[1])
            assertSame(fortiethSlot, contents[40])
        }
    }

    @Test
    fun `conflicting group share is skipped instead of written to either group or world profile`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "first",
                    setOf("source"),
                    setOf("last_location"),
                    MultiverseInventoryShares.standard,
                ),
                inventoryGroup(
                    "second",
                    setOf("source"),
                    setOf("last_location"),
                    MultiverseInventoryShares.standard,
                ),
            ),
        )
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(activeOptionalShares = setOf("last_location")),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onWorldChanged(player, "source", "destination")
        }

        verify(repository, never()).updateProfile(any(), any(), any(), any())
        verify(repository).updateGlobalPlayerData(any(), any())
    }

    @Test
    fun `world change within the same group does not save and reload the shared profile`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "shared",
                    setOf("world-a", "world-b"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service = InventoryTransitionService(repository, MultiverseInventoryConfig())

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onWorldChanged(player, "world-a", "world-b")
        }

        verify(repository, never()).readProfileResult(any())
        verify(repository, never()).updateProfile(any(), any(), any(), any())
        verify(repository).updateGlobalPlayerData(any(), any())
    }

    @Test
    fun `missing destination profile preserves current inventory on first use`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(inventoryGroup("survival", setOf("world"), setOf("inventory"))),
        )
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(applyPlayerDataOnJoin = true),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onPlayerJoined(player)
        }

        verify(player, never()).inventory
        verify(player, never()).enderChest
    }

    @Test
    fun `missing share field preserves current inventory`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(inventoryGroup("survival", setOf("world"), setOf("inventory"))),
        )
        whenever(repository.readProfileResult(any())).thenReturn(
            InventoryProfileReadResult.Present(
                JsonObject().apply {
                    add("SURVIVAL", JsonObject())
                },
            ),
        )
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(applyPlayerDataOnJoin = true),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onPlayerJoined(player)
        }

        verify(player, never()).inventory
        verify(player, never()).enderChest
    }

    @Test
    fun `malformed profile fails without applying inventory defaults`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(inventoryGroup("survival", setOf("world"), setOf("inventory"))),
        )
        whenever(repository.readProfileResult(any())).thenReturn(
            InventoryProfileReadResult.Malformed(IllegalArgumentException("Invalid JSON")),
        )
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(applyPlayerDataOnJoin = true),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            assertThrows(IllegalArgumentException::class.java) {
                service.onPlayerJoined(player)
            }
        }

        verify(player, never()).inventory
        verify(player, never()).enderChest
    }

    @Test
    fun `join reconciliation reads the current world instead of last-world metadata`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "destination-group",
                    setOf("destination"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        val world = mock<World>()
        whenever(world.name).thenReturn("destination")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(applyPlayerDataOnJoin = true),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onPlayerJoined(player)
        }

        verify(repository).readProfileResult(
            InventoryProfileLocation("groups", "destination-group", "Tester"),
        )
        verify(repository, never()).readGlobalProfile(any())
    }

    @Test
    fun `stored last location preserves orientation without recursively recording its teleport`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "location-group",
                    setOf("current"),
                    setOf("last_location"),
                    MultiverseInventoryShares.all - "last_location",
                ),
            ),
        )
        val storedProfile = JsonObject().apply {
            add(
                "SURVIVAL",
                JsonObject().apply {
                    add(
                        "lastLocation",
                        JsonObject().apply {
                            addProperty("==", "UnloadedWorldLocation")
                            addProperty("world", "stored")
                            addProperty("x", 12.0)
                            addProperty("y", 64.0)
                            addProperty("z", 8.0)
                            addProperty("yaw", 45.0)
                            addProperty("pitch", 10.0)
                        },
                    )
                },
            )
        }
        whenever(repository.readProfileResult(any())).thenReturn(
            InventoryProfileReadResult.Present(storedProfile),
        )
        val currentWorld = mock<World>()
        whenever(currentWorld.name).thenReturn("current")
        val storedWorld = mock<World>()
        whenever(storedWorld.name).thenReturn("stored")
        val currentLocation = Location(currentWorld, 0.0, 64.0, 0.0)
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(currentWorld)
        whenever(player.location).thenReturn(currentLocation)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        lateinit var service: InventoryTransitionService
        whenever(player.teleport(any<Location>())).thenAnswer {
            service.recordLastLocation(player, currentLocation)
            true
        }
        service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(
                    activeOptionalShares = setOf("last_location"),
                    applyLastLocationForAllTeleports = true,
                    applyPlayerDataOnJoin = true,
                ),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)
            bukkit.`when`<World> { Bukkit.getWorld("stored") }.thenReturn(storedWorld)

            service.onPlayerJoined(player)
        }

        val teleportedLocation = argumentCaptor<Location>()
        verify(player).teleport(teleportedLocation.capture())
        assertSame(storedWorld, teleportedLocation.firstValue.world)
        assertEquals(45.0f, teleportedLocation.firstValue.yaw)
        assertEquals(10.0f, teleportedLocation.firstValue.pitch)
        verify(repository, never()).updateProfile(any(), any(), any(), any())
    }

    @Test
    fun `preserves unknown entries and legacy field format when saving potion effects`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "potion-group",
                    setOf("world"),
                    setOf("potion_effects"),
                    MultiverseInventoryShares.all - "potion_effects",
                ),
            ),
        )
        val profile = JsonObject().apply {
            add(
                "SURVIVAL",
                JsonObject().apply {
                    addProperty("potions", """["future-effect"]""")
                },
            )
        }
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Present(profile))
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        whenever(player.inventory).thenReturn(mock())
        whenever(player.activePotionEffects).thenReturn(emptySet())
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(activeOptionalShares = setOf("potion_effects")),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onPlayerQuit(player)
        }

        val writtenFields = argumentCaptor<Map<String, JsonElement>>()
        verify(repository).updateProfile(
            eq(InventoryProfileLocation("groups", "potion-group", "Tester")),
            eq(InventoryProfileType.SURVIVAL),
            writtenFields.capture(),
            any(),
        )
        val encodedPotions = writtenFields.firstValue.getValue("potions")
        assertTrue(encodedPotions.isJsonPrimitive && encodedPotions.asJsonPrimitive.isString)
        assertEquals("""["future-effect"]""", encodedPotions.asString)
    }

    @Test
    fun `game-mode destination profile is applied only after the mode transition completes`() {
        listOf(
            GameMode.SURVIVAL to GameMode.CREATIVE,
            GameMode.CREATIVE to GameMode.SURVIVAL,
            GameMode.ADVENTURE to GameMode.SURVIVAL,
            GameMode.SURVIVAL to GameMode.SPECTATOR,
        ).forEach { (fromGameMode, toGameMode) ->
            assertGameModeTransition(fromGameMode, toGameMode)
        }
    }

    private fun assertGameModeTransition(
        fromGameMode: GameMode,
        toGameMode: GameMode,
    ) {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "survival",
                    setOf("world"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        val destinationProfile = JsonObject().apply {
            add(
                toGameMode.name,
                JsonObject().apply {
                    add(
                        "bedSpawnLocation",
                        JsonObject().apply {
                            addProperty("==", "RespawnLocation")
                            addProperty("world", "world")
                            addProperty("x", 12.0)
                            addProperty("y", 64.0)
                            addProperty("z", 8.0)
                            addProperty("yaw", 45.0)
                            addProperty("pitch", 10.0)
                            addProperty("respawnType", "UNKNOWN")
                        },
                    )
                },
            )
        }
        whenever(repository.readProfileResult(any())).thenReturn(
            InventoryProfileReadResult.Missing,
            InventoryProfileReadResult.Present(destinationProfile),
        )
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val playerInventory = mock<PlayerInventory>()
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.inventory).thenReturn(playerInventory)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.gameMode).thenReturn(toGameMode)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(
                    enableGamemodeShareHandling = true,
                    validateBedAnchorRespawnLocation = false,
                ),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)
            bukkit.`when`<World> { Bukkit.getWorld("world") }.thenReturn(world)

            val applyTransition =
                checkNotNull(service.onGameModeChanged(player, fromGameMode, toGameMode))
            verify(player, never()).setBedSpawnLocation(any(), eq(true))

            applyTransition()

            verify(player, times(1)).setBedSpawnLocation(any(), eq(true))
        }

        verify(repository).updateProfile(
            eq(InventoryProfileLocation("groups", "survival", "Tester")),
            eq(InventoryProfileType.valueOf(fromGameMode.name)),
            any(),
            any(),
        )
    }

    @Test
    fun `disabled game-mode sharing saves to the shared survival profile`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "survival",
                    setOf("world"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.CREATIVE)
        whenever(player.inventory).thenReturn(mock())
        val service = InventoryTransitionService(repository, MultiverseInventoryConfig())

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            assertNull(service.onGameModeChanged(player, GameMode.SURVIVAL, GameMode.CREATIVE))
            service.onPlayerQuit(player)
        }

        verify(repository).updateProfile(
            eq(InventoryProfileLocation("groups", "survival", "Tester")),
            eq(InventoryProfileType.SURVIVAL),
            any(),
            any(),
        )
    }

    @Test
    fun `respawn loads the destination profile once instead of processing the world event twice`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(emptyList())
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        val world = mock<World>()
        whenever(world.name).thenReturn("destination")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        whenever(player.isOnline).thenReturn(true)
        val service = InventoryTransitionService(repository, MultiverseInventoryConfig())

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            service.onPlayerRespawnScheduled(player, "destination")
            service.onWorldChanged(player, "source", "destination")
            service.onPlayerRespawned(player, "destination")
        }

        verify(repository, times(1)).readProfileResult(any())
        verify(repository, times(1)).updateGlobalPlayerData(any(), any())
    }

    @Test
    fun `respawn loads the destination group profile for the active game mode`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "destination-group",
                    setOf("destination"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        val profile = JsonObject().apply {
            add(
                "CREATIVE",
                JsonObject().apply {
                    add(
                        "bedSpawnLocation",
                        JsonObject().apply {
                            addProperty("==", "RespawnLocation")
                            addProperty("world", "destination")
                            addProperty("x", 12.0)
                            addProperty("y", 64.0)
                            addProperty("z", 8.0)
                            addProperty("yaw", 45.0)
                            addProperty("pitch", 10.0)
                            addProperty("respawnType", "UNKNOWN")
                        },
                    )
                },
            )
        }
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Present(profile))
        val world = mock<World>()
        whenever(world.name).thenReturn("destination")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.CREATIVE)
        whenever(player.isOnline).thenReturn(true)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(
                    enableGamemodeShareHandling = true,
                    validateBedAnchorRespawnLocation = false,
                ),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)
            bukkit.`when`<World> { Bukkit.getWorld("destination") }.thenReturn(world)

            service.onPlayerRespawnScheduled(player, "destination")
            service.onPlayerRespawned(player, "destination")
        }

        verify(repository).readProfileResult(
            InventoryProfileLocation("groups", "destination-group", "Tester"),
        )
        verify(player).setBedSpawnLocation(any(), eq(true))
    }

    @Test
    fun `invalid stored bed respawn location does not replace the live bed spawn`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "destination-group",
                    setOf("destination"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        val profile = JsonObject().apply {
            add(
                "SURVIVAL",
                JsonObject().apply {
                    add(
                        "bedSpawnLocation",
                        JsonObject().apply {
                            addProperty("==", "RespawnLocation")
                            addProperty("world", "destination")
                            addProperty("x", 12.0)
                            addProperty("y", 64.0)
                            addProperty("z", 8.0)
                            addProperty("respawnType", "BED")
                        },
                    )
                },
            )
        }
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Present(profile))
        val world = mock<World>()
        whenever(world.name).thenReturn("destination")
        whenever(world.getBlockAt(any<Location>())).thenReturn(mock())
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        whenever(player.isOnline).thenReturn(true)
        val service =
            InventoryTransitionService(
                repository,
                MultiverseInventoryConfig(validateBedAnchorRespawnLocation = true),
            )

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)
            bukkit.`when`<World> { Bukkit.getWorld("destination") }.thenReturn(world)

            service.onPlayerRespawnScheduled(player, "destination")
            service.onPlayerRespawned(player, "destination")
        }

        verify(player, never()).setBedSpawnLocation(any(), eq(true))
        verify(repository).updateGlobalPlayerData(any(), any())
    }

    @Test
    fun `failed profile write does not advance global world metadata`() {
        val repository = mock<InventoryRepository>()
        whenever(repository.profileName(any(), any())).thenReturn("Tester")
        whenever(repository.readWorldGroups()).thenReturn(
            listOf(
                inventoryGroup(
                    "survival",
                    setOf("world"),
                    setOf("bed_spawn"),
                    MultiverseInventoryShares.standard - "bed_spawn",
                ),
            ),
        )
        whenever(repository.readProfileResult(any())).thenReturn(InventoryProfileReadResult.Missing)
        doThrow(IllegalStateException("write failed"))
            .whenever(repository)
            .updateProfile(any(), any(), any(), any())
        val world = mock<World>()
        whenever(world.name).thenReturn("world")
        val player = mock<Player>()
        whenever(player.uniqueId).thenReturn(UUID.fromString("123e4567-e89b-12d3-a456-426614174000"))
        whenever(player.name).thenReturn("Tester")
        whenever(player.world).thenReturn(world)
        whenever(player.gameMode).thenReturn(GameMode.SURVIVAL)
        whenever(player.inventory).thenReturn(mock())
        val service = InventoryTransitionService(repository, MultiverseInventoryConfig())

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)

            assertThrows(IllegalStateException::class.java) {
                service.onPlayerQuit(player)
            }
        }

        verify(repository, never()).updateGlobalPlayerData(any(), any())
    }

    private fun inventoryGroup(
        name: String,
        worlds: Set<String>,
        shares: Set<String>,
        disabledShares: Set<String> = emptySet(),
    ) = InventoryWorldGroup(
        name = name,
        worlds = worlds,
        shares = shares,
        disabledShares = disabledShares,
        spawnWorld = null,
        spawnPriority = null,
    )
}

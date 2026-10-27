package com.projectcitybuild.pcbridge.runtime.teleportation.storage

import com.google.gson.JsonElement
import com.google.gson.reflect.TypeToken
import com.projectcitybuild.pcbridge.core.filestorage.JsonFileStorage
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.serialization.SerializableLocation
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import com.projectcitybuild.pcbridge.runtime.teleportation.PlayerTeleporter
import com.projectcitybuild.pcbridge.runtime.teleportation.events.PlayerPreTeleportEvent
import com.projectcitybuild.pcbridge.runtime.teleportation.exceptions.TeleportFailedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.UUID
import java.util.concurrent.CompletableFuture

class TeleportHistoryStorageTest {
    @TempDir
    lateinit var tempDir: File

    @Test
    fun `teleport history state round trips through Store gson persistence`() = runBlocking {
        val storeFile = tempDir.resolve("server_state.json")
        val location = SerializableLocation(
            worldId = "51a62d2d-08e8-4bfa-938c-64ba71c4b81c",
            worldName = "world",
            x = 12.5,
            y = 64.0,
            z = -8.25,
            yaw = 90.0f,
            pitch = -15.0f,
        )
        val playerId = UUID.randomUUID()
        val store = makeStore(storeFile)
        store.hydrate()

        store.mutate(teleportHistoryStateKey) {
            it.copy(previousLocations = mapOf(playerId.toString() to location))
        }
        store.persist()

        val restoredStore = makeStore(storeFile)
        restoredStore.hydrate()

        assertEquals(
            TeleportHistoryState(mapOf(playerId.toString() to location)),
            restoredStore.state(teleportHistoryStateKey),
        )
    }

    @Test
    fun `successful moves save the previous location and back movement toggles history`() = runBlocking {
        val store = makeStore(tempDir.resolve("server_state.json"))
        val server = mock<Server>()
        val pluginManager = mock<PluginManager>()
        val world = mock<World>()
        val worldId = UUID.fromString("51a62d2d-08e8-4bfa-938c-64ba71c4b81c")
        val playerId = UUID.randomUUID()
        whenever(world.uid).thenReturn(worldId)
        whenever(world.name).thenReturn("world")
        whenever(server.pluginManager).thenReturn(pluginManager)
        whenever(server.getWorld(worldId)).thenReturn(world)
        val historyStorage = TeleportHistoryStorage(
            store = store,
            eventBroadcaster = SpigotEventBroadcaster(server) { Dispatchers.Unconfined },
            server = server,
        )
        val player = mock<Player>()
        var currentLocation = Location(world, 1.0, 65.0, 1.0)
        whenever(player.uniqueId).thenReturn(playerId)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.location).thenAnswer { currentLocation }
        whenever(player.teleportAsync(any<Location>(), any<TeleportCause>())).thenAnswer { invocation ->
            currentLocation = invocation.getArgument<Location>(0).clone()
            CompletableFuture.completedFuture(true)
        }
        val teleporter = PlayerTeleporter(mock(), historyStorage, server, Dispatchers.Unconfined)
        val destination = Location(world, 20.25, 70.0, -4.75, 25.0f, 5.0f)

        teleporter.move(
            player = player,
            destination = destination,
            options = PlayerTeleporter.TeleportOptions(preloadDestinationChunk = false, playSound = false),
        )

        val firstBackLocation = assertInstanceOf(
            TeleportHistoryStorage.Lookup.Found::class.java,
            historyStorage.retrieve(playerId),
        ).location
        assertEquals(Location(world, 1.0, 65.0, 1.0), firstBackLocation)

        teleporter.move(
            player = player,
            destination = firstBackLocation,
            options = PlayerTeleporter.TeleportOptions(preloadDestinationChunk = false, playSound = false),
        )

        val secondBackLocation = assertInstanceOf(
            TeleportHistoryStorage.Lookup.Found::class.java,
            historyStorage.retrieve(playerId),
        ).location
        assertEquals(destination, secondBackLocation)
        val events = argumentCaptor<Event>()
        verify(pluginManager, times(2)).callEvent(events.capture())
        events.allValues.forEach { assertInstanceOf(PlayerPreTeleportEvent::class.java, it) }
    }

    @Test
    fun `failed teleports do not save history or dispatch the pre-teleport event`() = runBlocking {
        val store = makeStore(tempDir.resolve("server_state.json"))
        val server = mock<Server>()
        val pluginManager = mock<PluginManager>()
        val world = mock<World>()
        val playerId = UUID.randomUUID()
        val historyStorage = TeleportHistoryStorage(
            store = store,
            eventBroadcaster = SpigotEventBroadcaster(server) { Dispatchers.Unconfined },
            server = server,
        )
        val player = mock<Player>()
        whenever(server.pluginManager).thenReturn(pluginManager)
        whenever(player.uniqueId).thenReturn(playerId)
        whenever(player.isOnline).thenReturn(true)
        whenever(player.location).thenReturn(Location(world, 1.0, 65.0, 1.0))
        whenever(player.teleportAsync(any<Location>(), any<TeleportCause>()))
            .thenReturn(CompletableFuture.completedFuture(false))
        val teleporter = PlayerTeleporter(mock(), historyStorage, server, Dispatchers.Unconfined)

        var failed = false
        try {
            teleporter.move(
                player = player,
                destination = Location(world, 20.0, 70.0, -4.0),
                options = PlayerTeleporter.TeleportOptions(preloadDestinationChunk = false),
            )
        } catch (_: TeleportFailedException) {
            failed = true
        }

        assertTrue(failed)
        assertEquals(TeleportHistoryStorage.Lookup.Missing, historyStorage.retrieve(playerId))
        verify(pluginManager, never()).callEvent(any())
    }

    @Test
    fun `stored malformed world uuid returns unavailable history`() = runBlocking {
        val store = makeStore(tempDir.resolve("server_state.json"))
        val server = mock<Server>()
        val playerId = UUID.randomUUID()
        store.mutate(teleportHistoryStateKey) { state ->
            state.copy(
                previousLocations = mapOf(
                    playerId.toString() to SerializableLocation(
                        worldId = "bad-world-id",
                        worldName = "fallback-world",
                        x = 0.0,
                        y = 64.0,
                        z = 0.0,
                        yaw = 0.0f,
                        pitch = 0.0f,
                    ),
                ),
            )
        }
        val historyStorage = TeleportHistoryStorage(
            store = store,
            eventBroadcaster = SpigotEventBroadcaster(server) { Dispatchers.Unconfined },
            server = server,
        )

        assertEquals(TeleportHistoryStorage.Lookup.Unavailable, historyStorage.retrieve(playerId))
    }

    private fun makeStore(file: File) = Store(
        file = file,
        fileStorage = JsonFileStorage(
            typeToken = object : TypeToken<Map<String, JsonElement>>() {},
        ),
    )
}

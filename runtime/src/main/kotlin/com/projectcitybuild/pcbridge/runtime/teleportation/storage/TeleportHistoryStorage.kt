package com.projectcitybuild.pcbridge.runtime.teleportation.storage

import com.projectcitybuild.pcbridge.core.observability.logging.log
import com.projectcitybuild.pcbridge.platform.paper.events.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.platform.paper.serialization.SerializableLocation
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import com.projectcitybuild.pcbridge.runtime.teleportation.events.PlayerPreTeleportEvent
import org.bukkit.Location
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.UUID

/**
 * Stores the latest previous location in [Store], which is persisted during normal shutdown.
 * A crash before persistence can lose recent history changes.
 */
class TeleportHistoryStorage(
    private val store: Store,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val server: Server,
) {
    sealed interface Lookup {
        data class Found(val location: Location) : Lookup

        data object Missing : Lookup

        data object Unavailable : Lookup
    }

    suspend fun put(
        prev: Location,
        player: Player,
    ) {
        eventBroadcaster.broadcast(
            PlayerPreTeleportEvent(player),
        )

        val world = prev.world
        if (world == null) {
            log.warn { "Could not save teleport history for ${player.uniqueId}: previous world is unavailable" }
            return
        }

        val serializedLocation = SerializableLocation.fromLocation(prev, world)
        store.mutate(teleportHistoryStateKey) { state ->
            state.copy(
                previousLocations = state.previousLocations + (player.uniqueId.toString() to serializedLocation),
            )
        }
    }

    fun retrieve(playerId: UUID): Lookup {
        val previousLocation = store.state(teleportHistoryStateKey)
            .previousLocations[playerId.toString()]
            ?: return Lookup.Missing
        val location = previousLocation.toLocation(server) ?: return Lookup.Unavailable
        return Lookup.Found(location)
    }
}

package com.projectcitybuild.pcbridge.paper.architecture.state.listeners

import com.projectcitybuild.pcbridge.paper.architecture.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateDestroyedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent

class PlayerStateListener(
    private val store: Store,
    private val time: LocalizedTime,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : Listener {
    /**
     * Creates a PlayerState for the connecting user
     */
    @EventHandler
    suspend fun onConnectionPermitted(event: ConnectionPermittedEvent) {
        log.info { "Creating player state for ${event.playerUUID}" }

        val playerState = event.playerData?.let {
            PlayerState.fromPlayerData(it, connectedAt = time.now())
        } ?: PlayerState(connectedAt = time.now())

        store.mutate { state ->
            state.copy(players = state.players.apply { put(event.playerUUID, playerState) })
        }
        eventBroadcaster.broadcast(
            PlayerStateUpdatedEvent(
                prevState = null,
                state = playerState,
                playerUUID = event.playerUUID,
            ),
        )

        // TODO: warn the user if their data was unable to be fetched
    }

    /**
     * Destroys PlayerState for the disconnecting user.
     *
     * Note: Runs at highest priority so that it's invoked last
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerQuit(event: PlayerQuitEvent) {
        val uuid = event.player.uniqueId
        log.info { "Destroying player state for $uuid" }
        val prevState = store.state.players[uuid]

        store.mutate { state ->
            state.copy(players = state.players.apply { remove(uuid) })
        }
        eventBroadcaster.broadcast(
            PlayerStateDestroyedEvent(
                playerData = prevState,
                playerUUID = uuid,
            ),
        )
    }
}
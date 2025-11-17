package com.projectcitybuild.pcbridge.paper.architecture.state.listeners

import com.projectcitybuild.pcbridge.paper.architecture.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateDestroyedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.observability.errors.ErrorTracker
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerStateListener(
    private val store: Store,
    private val time: LocalizedTime,
    private val eventBroadcaster: SpigotEventBroadcaster,
    private val errorTracker: ErrorTracker,
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
    }

    /**
     * Emits that PlayerState exists for the joining player
     */
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        // TODO: warn the user if their data was unable to be fetched

        val playerState = store.state.players[event.player.uniqueId]
        if (playerState == null) {
            log.error { "Player state was missing on join event" }
            errorTracker.report(Exception("Player state was missing on join event"))
            return
        }
        // Some state update listeners require an actual Player to exist, and this is
        // only present during and after the PlayerJoinEvent
        eventBroadcaster.broadcast(
            PlayerStateCreatedEvent(
                state = playerState,
                playerUUID = event.player.uniqueId,
            ),
        )
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

        val exists = store.state.players.containsKey(event.player.uniqueId)
        if (!exists) {
            log.debug { "Player state did not exist - no clean up needed" }
            return
        }
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
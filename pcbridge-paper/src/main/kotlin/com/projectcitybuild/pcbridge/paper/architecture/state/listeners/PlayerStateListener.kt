package com.projectcitybuild.pcbridge.paper.architecture.state.listeners

import com.projectcitybuild.pcbridge.paper.architecture.connection.connectionTracer
import com.projectcitybuild.pcbridge.paper.architecture.connection.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.paper.architecture.listeners.scoped
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSession
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateDestroyedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.stateTracer
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.jvm.java

class PlayerStateListener(
    private val session: SessionStore,
    private val time: LocalizedTime,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : Listener {
    @EventHandler(
        priority = EventPriority.MONITOR,
        ignoreCancelled = true,
    )
    suspend fun onConnectionPermitted(
        event: ConnectionPermittedEvent,
    ) = event.scoped(connectionTracer, this::class.java) {
        log.info { "Creating player state for ${event.playerUUID}" }

        if (event.playerData == null) {
            log.warn { "Player ${event.playerUUID} entered the server but player data unavailable" }
        }
        val playerSession = PlayerSession.fromPlayerData(
            event.playerData,
            connectedAt = time.now(),
        )
        session.mutate { state ->
            val updatedPlayers = state.players + mapOf(event.playerUUID to playerSession)
            state.copy(players = updatedPlayers)
        }
        eventBroadcaster.broadcast(
            PlayerStateCreatedEvent(
                state = session.state.players[event.playerUUID]!!,
                playerUUID = event.playerUUID,
            ),
        )
    }

    /**
     * Warns the player if we failed to fetch their data
     */
    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerJoin(
        event: PlayerJoinEvent
    ) = event.scoped(stateTracer, this::class.java) {
        // TODO: warn the user if their data was unable to be fetched

        val playerState = session.state.players[event.player.uniqueId]
        if (playerState == null) {
            log.warn { "Player state was missing on join event" }
            event.player.sendRichMessage(
                "Warning: Unable to retrieve player data. Your permissions, rank and other data may be missing. Please type /sync or try reconnect in a moment"
            )
        }
    }

    /**
     * Destroys PlayerState for the disconnecting user.
     *
     * Note: Runs at highest priority so that it's invoked last
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    suspend fun onPlayerQuit(
        event: PlayerQuitEvent,
    ) = event.scoped(stateTracer, this::class.java) {
        val uuid = event.player.uniqueId
        log.info { "Destroying player state for $uuid" }
        val prevState = session.state.players[uuid]

        val exists = session.state.players.containsKey(event.player.uniqueId)
        if (!exists) {
            log.debug { "Player state did not exist - no clean up needed" }
            return@scoped
        }
        session.mutate { state ->
            state.copy(players = state.players.filter { it.key != uuid })
        }
        eventBroadcaster.broadcast(
            PlayerStateDestroyedEvent(
                playerData = prevState,
                playerUUID = uuid,
            ),
        )
    }
}
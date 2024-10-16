package com.projectcitybuild.pcbridge.features.playerstate.listeners

import com.projectcitybuild.pcbridge.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.core.state.PlayerState
import com.projectcitybuild.pcbridge.core.state.Store
import com.projectcitybuild.pcbridge.features.bans.events.ConnectionPermittedEvent
import com.projectcitybuild.pcbridge.features.playerstate.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.features.playerstate.events.PlayerStateDestroyedEvent
import kotlinx.coroutines.withContext
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import kotlin.coroutines.CoroutineContext

/**
 * Listens to connection events and creates/destroys the connecting player's state
 */
class PlayerStateListener(
    private val store: Store,
    private val server: Server,
    private val time: LocalizedTime,
    private val minecraftDispatcher: () -> CoroutineContext,
) : Listener {
    /**
     * Creates a PlayerState for the connecting user
     */
    @EventHandler
    suspend fun onConnectionPermitted(event: ConnectionPermittedEvent) {
        log.info { "Creating player state for ${event.playerUUID}" }

        val data = event.playerData
        val playerState = PlayerState(
            connectedAt = time.now(),
            account = data.account,
            badges = data.badges,
            donationPerks = data.donationPerks,
        )
        store.mutate { state ->
            state.copy(players = state.players.apply { put(event.playerUUID, playerState) })
        }
        withContext(minecraftDispatcher()) {
            server.pluginManager.callEvent(
                PlayerStateCreatedEvent(
                    playerData = event.playerData,
                    playerUUID = event.playerUUID,
                ),
            )
        }
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
        withContext(minecraftDispatcher()) {
            server.pluginManager.callEvent(
                PlayerStateDestroyedEvent(
                    playerData = prevState,
                    playerUUID = uuid,
                ),
            )
        }
    }
}
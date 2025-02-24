package com.projectcitybuild.pcbridge.paper.architecture.state.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.connection.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class PlayerSyncRequestListener(
    private val store: Store,
    private val time: LocalizedTime,
    private val playerRepository: PlayerRepository,
    private val server: Server,
    private val eventBroadcaster: SpigotEventBroadcaster,
) : Listener {
    @EventHandler
    suspend fun onSyncRequested(event: PlayerSyncRequestedEvent) {
        val matchingPlayer = server.onlinePlayers.firstOrNull { it.uniqueId == event.playerUUID }
        if (matchingPlayer == null) {
            log.info { "Skipping sync, player (${event.playerUUID}) not found" }
            return
        }

        log.info { "Creating player state for ${event.playerUUID}" }

        val playerData = playerRepository.get(
            uuid = matchingPlayer.uniqueId,
            ip = matchingPlayer.address?.address,
        )
        val playerState = PlayerState.fromPlayerData(
            playerData,
            connectedAt = time.now(),
        )
        val prevState = store.state.players[event.playerUUID]
        store.mutate { state ->
            state.copy(players = state.players.apply { put(event.playerUUID, playerState) })
        }
        eventBroadcaster.broadcast(
            PlayerStateUpdatedEvent(
                prevState = prevState,
                state = playerState,
                playerUUID = event.playerUUID,
            ),
        )
        matchingPlayer.sendMessage(
            MiniMessage.miniMessage().deserialize("<green>Your account has been synced</green>")
        )
    }
}
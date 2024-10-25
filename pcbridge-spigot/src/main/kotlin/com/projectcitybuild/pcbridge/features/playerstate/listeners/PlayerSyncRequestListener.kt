package com.projectcitybuild.pcbridge.features.playerstate.listeners

import com.projectcitybuild.pcbridge.core.datetime.LocalizedTime
import com.projectcitybuild.pcbridge.core.logger.log
import com.projectcitybuild.pcbridge.core.store.PlayerState
import com.projectcitybuild.pcbridge.core.store.Store
import com.projectcitybuild.pcbridge.features.bans.repositories.PlayerRepository
import com.projectcitybuild.pcbridge.features.playerstate.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.features.groups.events.PlayerSyncRequestedEvent
import com.projectcitybuild.pcbridge.support.spigot.SpigotEventBroadcaster
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
            playerUUID = matchingPlayer.uniqueId,
            ip = matchingPlayer.address.address,
        )
        val playerState = PlayerState.fromPlayerData(
            playerData,
            connectedAt = time.now(),
        )
        store.mutate { state ->
            state.copy(players = state.players.apply { put(event.playerUUID, playerState) })
        }
        eventBroadcaster.broadcast(
            PlayerStateUpdatedEvent(
                state = playerState,
                playerUUID = event.playerUUID,
            ),
        )
        matchingPlayer.sendMessage(
            MiniMessage.miniMessage().deserialize("<color:green>Your account has been synced</color>")
        )
    }
}
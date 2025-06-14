package com.projectcitybuild.pcbridge.paper.features.sync.actions

import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerState
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.features.sync.repositories.PlayerRepository
import org.bukkit.Server
import java.util.UUID

class SyncPlayer(
    private val store: Store,
    private val time: LocalizedTime,
    private val playerRepository: PlayerRepository,
    private val server: Server,
    private val eventBroadcaster: SpigotEventBroadcaster,
) {
    suspend fun execute(playerUUID: UUID) {
        val matchingPlayer = server.onlinePlayers.firstOrNull { it.uniqueId == playerUUID }
        if (matchingPlayer == null) {
            log.info { "Skipping sync, player ($playerUUID) not found" }
            return
        }

        log.info { "Creating player state for $playerUUID" }

        val playerData = playerRepository.get(
            uuid = matchingPlayer.uniqueId,
            ip = matchingPlayer.address?.address,
        )
        val playerState = PlayerState.fromPlayerData(
            playerData,
            connectedAt = time.now(),
        )
        val prevState = store.state.players[playerUUID]
        store.mutate { state ->
            state.copy(players = state.players.apply { put(playerUUID, playerState) })
        }
        eventBroadcaster.broadcast(
            PlayerStateUpdatedEvent(
                prevState = prevState,
                state = playerState,
                playerUUID = playerUUID,
            ),
        )
        matchingPlayer.sendRichMessage("<green>Your account has been synced</green>")
    }
}

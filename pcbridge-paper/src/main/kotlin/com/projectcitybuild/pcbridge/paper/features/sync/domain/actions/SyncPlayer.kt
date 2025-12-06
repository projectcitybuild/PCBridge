package com.projectcitybuild.pcbridge.paper.features.sync.domain.actions

import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.data.PlayerSession
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.datetime.services.LocalizedTime
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.core.libs.store.SessionStore
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotEventBroadcaster
import com.projectcitybuild.pcbridge.paper.core.support.spigot.extensions.onlinePlayer
import com.projectcitybuild.pcbridge.paper.features.sync.domain.repositories.PlayerRepository
import org.bukkit.Server
import java.util.UUID

class SyncPlayer(
    private val session: SessionStore,
    private val time: LocalizedTime,
    private val playerRepository: PlayerRepository,
    private val server: Server,
    private val eventBroadcaster: SpigotEventBroadcaster,
) {
    suspend fun execute(playerUUID: UUID) {
        val matchingPlayer = server.onlinePlayer(uuid = playerUUID)
        if (matchingPlayer == null) {
            log.info { "Skipping sync, player ($playerUUID) not found" }
            return
        }

        log.info { "Creating player state for $playerUUID" }

        val playerData = playerRepository.get(
            uuid = matchingPlayer.uniqueId,
            ip = matchingPlayer.address?.address,
        )
        val playerSession = PlayerSession.fromPlayerData(
            playerData,
            connectedAt = time.now(),
        )
        val prevState = session.state.players[playerUUID]
        session.mutate { state ->
            state.copy(players = state.players + mapOf(playerUUID to playerSession))
        }
        eventBroadcaster.broadcast(
            PlayerStateUpdatedEvent(
                prevState = prevState,
                state = playerSession,
                playerUUID = playerUUID,
            ),
        )
        matchingPlayer.sendRichMessage("<green>Your account has been synced</green>")
    }
}

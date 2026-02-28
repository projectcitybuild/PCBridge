package com.projectcitybuild.pcbridge.paper.features.roles.hooks.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.roles.domain.repositories.ChatRoleRepository
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import java.util.UUID

class TabRolesPlaceholder(
    private val chatRoleRepository: ChatRoleRepository,
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "groups"

    override suspend fun value(player: Player): Component {
        return chatRoleRepository.getRolesComponent(player.uniqueId).value?.appendSpace()
            ?: Component.empty()
    }

    @EventHandler
    suspend fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        log.debug { "PlayerStateCreatedEvent: updating tab roles for player" }
        update(event.playerUUID)
    }

    @EventHandler
    suspend fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.syncedValue?.roles == event.state.syncedValue?.roles) {
            return
        }
        log.debug { "PlayerStateUpdatedEvent: updating tab roles for player" }
        update(event.playerUUID)
    }

    private suspend fun update(playerUUID: UUID) {
        server.getPlayer(playerUUID)?.let { player ->
            tabRenderer.updatePlayerName(player)
        }
    }
}
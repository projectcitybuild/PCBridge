package com.projectcitybuild.pcbridge.paper.features.groups.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.groups.repositories.ChatGroupRepository
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

class TabGroupsPlaceholder(
    private val chatGroupRepository: ChatGroupRepository,
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "groups"

    override suspend fun value(player: Player): Component {
        return chatGroupRepository.getGroupsComponent(player.uniqueId).value?.appendSpace()
            ?: Component.empty()
    }

    @EventHandler
    suspend fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.groups == event.state.groups) return

        log.debug { "PlayerStateUpdatedEvent: updating tab groups for player" }

        server.getPlayer(event.playerUUID)?.let { player ->
            tabRenderer.updatePlayerName(player)
        }
    }
}
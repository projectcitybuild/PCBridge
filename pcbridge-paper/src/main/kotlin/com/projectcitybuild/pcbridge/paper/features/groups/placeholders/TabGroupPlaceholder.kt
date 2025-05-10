package com.projectcitybuild.pcbridge.paper.features.groups.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.features.groups.RolesFilter
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

class TabGroupPlaceholder(
    private val rolesFilter: RolesFilter,
    private val store: Store,
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "groups"

    override suspend fun value(player: Player): Component {
        val playerState = store.state.players[player.uniqueId]
        val roles = rolesFilter.filter(playerState?.groups?.toSet() ?: emptySet())
        val roleNames = roles.values.mapNotNull { it.minecraftName }

        return Component.text(
            if (playerState == null) "Unknown"
            else if (roleNames.isEmpty()) "Guest"
            else roleNames.joinToString(separator = ", ")
        )
    }

    @EventHandler
    suspend fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        log.debug { "PlayerStateUpdatedEvent: updating tab groups for player" }

        server.getPlayer(event.playerUUID)?.let { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }
}
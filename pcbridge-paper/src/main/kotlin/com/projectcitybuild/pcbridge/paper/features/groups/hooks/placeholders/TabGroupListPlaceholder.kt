package com.projectcitybuild.pcbridge.paper.features.groups.hooks.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateCreatedEvent
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.groups.domain.RolesFilter
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import java.util.UUID

class TabGroupListPlaceholder(
    private val rolesFilter: RolesFilter,
    private val store: Store,
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "group_list"

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
    suspend fun onPlayerStateCreated(event: PlayerStateCreatedEvent) {
        log.debug { "PlayerStateCreatedEvent: updating tab group list for player" }
        update(event.playerUUID)
    }

    @EventHandler
    suspend fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.groups == event.state.groups) return

        log.debug { "PlayerStateUpdatedEvent: updating tab group list for player" }
        update(event.playerUUID)
    }

    private suspend fun update(playerUUID: UUID) {
        server.getPlayer(playerUUID)?.let { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }
}
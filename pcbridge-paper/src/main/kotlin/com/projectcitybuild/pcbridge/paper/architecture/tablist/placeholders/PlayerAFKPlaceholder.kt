package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.architecture.state.events.PlayerStateUpdatedEvent
import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler

class PlayerAFKPlaceholder(
    private val server: Server,
    private val tabRenderer: TabRenderer,
    private val store: Store,
): UpdatableTabPlaceholder {
    override val placeholder: String = "afk"

    override suspend fun value(player: Player): Component {
        val miniMessage = MiniMessage.miniMessage()
        val state = store.state.players[player.uniqueId]
        if (state != null && state.afk) {
            return miniMessage.deserialize(" <gray>AFK</gray>")
        }
        return Component.empty()
    }

    @EventHandler
    suspend fun onPlayerStateUpdated(event: PlayerStateUpdatedEvent) {
        if (event.prevState?.afk == event.state.afk) return

        deprecatedLog.debug { "PlayerStateUpdatedEvent: updating tab AFK placeholder for player" }

        server.getPlayer(event.playerUUID)?.let { player ->
            tabRenderer.updatePlayerName(player)
        }
    }
}
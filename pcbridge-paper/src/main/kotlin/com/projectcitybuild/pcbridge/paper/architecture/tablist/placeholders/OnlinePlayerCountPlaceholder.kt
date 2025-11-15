package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class OnlinePlayerCountPlaceholder(
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "player_count"

    override suspend fun value(player: Player): Component
        = Component.text(server.onlinePlayers.size)

    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        log.debug { "PlayerJoinEvent: updating all player tabs" }

        server.onlinePlayers.forEach { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }

    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerQuit(event: PlayerQuitEvent) {
        log.debug { "PlayerQuitEvent: updating all player tabs" }

        server.onlinePlayers.forEach { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }
}
package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class OnlinePlayerCountPlaceholder(
    private val server: Server,
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "%player_count%"

    override suspend fun value(player: Player): Component
        = Component.text(server.onlinePlayers.size)

    @EventHandler
    suspend fun onPlayerJoin(event: PlayerJoinEvent) {
        server.onlinePlayers.forEach { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }

    @EventHandler
    suspend fun onPlayerQuit(event: PlayerQuitEvent) {
        server.onlinePlayers.forEach { player ->
            tabRenderer.updateHeaderAndFooter(player)
        }
    }
}
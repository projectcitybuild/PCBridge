package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.deprecatedLog
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent

class PlayerWorldPlaceholder(
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "world"

    override suspend fun value(player: Player): Component
        = Component.text(player.location.world.name)

    @EventHandler(ignoreCancelled = true)
    suspend fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        deprecatedLog.debug { "PlayerChangedWorldEvent: updating tab for player" }

        tabRenderer.updateHeaderAndFooter(event.player)
    }
}
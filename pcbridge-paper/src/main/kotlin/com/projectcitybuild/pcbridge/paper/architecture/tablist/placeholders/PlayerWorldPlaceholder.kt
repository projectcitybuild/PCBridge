package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabRenderer
import com.projectcitybuild.pcbridge.paper.architecture.tablist.UpdatableTabPlaceholder
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent

class PlayerWorldPlaceholder(
    private val tabRenderer: TabRenderer,
): UpdatableTabPlaceholder {
    override val placeholder: String = "%world%"

    override suspend fun value(player: Player): Component
        = Component.text(player.location.world.name)

    @EventHandler
    suspend fun onPlayerChangedWorld(event: PlayerChangedWorldEvent) {
        tabRenderer.updateHeaderAndFooter(event.player)
    }
}
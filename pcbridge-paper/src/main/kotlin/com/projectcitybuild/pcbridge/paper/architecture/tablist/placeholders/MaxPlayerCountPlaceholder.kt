package com.projectcitybuild.pcbridge.paper.architecture.tablist.placeholders

import com.projectcitybuild.pcbridge.paper.architecture.tablist.TabPlaceholder
import net.kyori.adventure.text.Component
import org.bukkit.Server
import org.bukkit.entity.Player

class MaxPlayerCountPlaceholder(
    private val server: Server,
): TabPlaceholder {
    override val placeholder: String = "%max_players%"

    override suspend fun value(player: Player): Component
        = Component.text(server.maxPlayers)
}
package com.projectcitybuild.pcbridge.paper.architecture.tablist

import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.component.deserialize
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import org.bukkit.entity.Player

class TabRenderer(
    private val remoteConfig: RemoteConfig,
    private val tabPlaceholders: TabPlaceholders,
) {
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Updates the tab name of the given player
     */
    suspend fun updatePlayerName(player: Player) {
        val config = remoteConfig.latest.config.tab

        val placeholders = tabPlaceholders.player.map { it.resolve(player) }

        val name = miniMessage.deserialize(
            config.player,
            placeholders,
        )
        player.playerListName(name)
    }

    /**
     * Re-renders the tab header and footer for the given player
     */
    suspend fun updateHeaderAndFooter(player: Player) {
        val config = remoteConfig.latest.config.tab

        val placeholders = tabPlaceholders.section.map { it.resolve(player) }

        val header = miniMessage.deserialize(
            config.header.joinToString(separator = "<newline>"),
            placeholders,
        )
        val footer = miniMessage.deserialize(
            config.footer.joinToString(separator = "<newline>"),
            placeholders,
        )
        player.sendPlayerListHeaderAndFooter(header, footer)
    }
}

private suspend fun TabPlaceholder.resolve(player: Player)
    = Placeholder.component(placeholder, value(player))

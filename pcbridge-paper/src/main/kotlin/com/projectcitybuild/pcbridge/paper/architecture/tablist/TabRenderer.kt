package com.projectcitybuild.pcbridge.paper.architecture.tablist

import com.projectcitybuild.pcbridge.paper.core.libs.logger.log
import com.projectcitybuild.pcbridge.paper.core.libs.remoteconfig.RemoteConfig
import com.projectcitybuild.pcbridge.paper.core.support.component.deserialize
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.entity.Player
import kotlin.math.max

class TabRenderer(
    private val remoteConfig: RemoteConfig,
    private val tabPlaceholders: TabPlaceholders,
) {
    private val miniMessage = MiniMessage.miniMessage()

    /**
     * Updates the tab name of the given player
     */
    suspend fun updatePlayerName(player: Player) {
        log.debug { "Updating tab player name for ${player.uniqueId}" }

        val config = remoteConfig.latest.config.tab

        val initialPlaceholders = tabPlaceholders
            .player
            .map { it.resolve(player) }
            .toMutableSet()

        val name = miniMessage.deserialize(
            config.player,
            initialPlaceholders.apply {
                add(Placeholder.component("spacer", Component.empty()))
            },
        )

        // Determine how many visible characters are present so that
        // we can provide a <spacer> placeholder to fill available space
        val serializer = PlainTextComponentSerializer.plainText()
        val unformatted = serializer.serialize(name)
        val availableSpace = max(0, config.playerColumnLength - unformatted.length)

        val finalizedName = miniMessage.deserialize(
            config.player,
            initialPlaceholders.apply {
                add(Placeholder.component("spacer", Component.text(" ".repeat(availableSpace))))
            },
        )
        player.playerListName(finalizedName)
    }

    /**
     * Re-renders the tab header and footer for the given player
     */
    suspend fun updateHeaderAndFooter(player: Player) {
        log.debug { "Updating tab header and footer for ${player.uniqueId}" }

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

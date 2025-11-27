package com.projectcitybuild.pcbridge.paper.architecture.tablist

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
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
        log.trace { "Updating tab player name for ${player.uniqueId}" }

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
        val textLength = MinecraftFontWidths.getStringWidth(unformatted)
        val totalPaddingWidth = config.playerColumnLength - textLength
        val figureSpace = '\u2007' // figure space ≈ 6 pixels wide in Minecraft
        val spacePixelWidth = 6
        val numSpaces = max(0, totalPaddingWidth / spacePixelWidth)

        val finalizedName = miniMessage.deserialize(
            config.player,
            initialPlaceholders.apply {
                add(Placeholder.component(
                    "spacer",
                    Component.text(figureSpace.toString().repeat(numSpaces))),
                )
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

private object MinecraftFontWidths {
    // Minecraft does not use a monospaced font, so we need to approximate
    // the width of characters in order to provide text-alignment
    private val charWidths = mapOf(
        ' ' to 4, '!' to 2, '"' to 5, '#' to 6, '$' to 6,
        '%' to 6, '&' to 6, '\'' to 3, '(' to 5, ')' to 5,
        '*' to 5, '+' to 6, ',' to 2, '-' to 6, '.' to 2,
        '/' to 6, '0' to 6, '1' to 6, '2' to 6, '3' to 6,
        '4' to 6, '5' to 6, '6' to 6, '7' to 6, '8' to 6,
        '9' to 6, ':' to 2, ';' to 2, '<' to 5, '=' to 6,
        '>' to 5, '?' to 6, '@' to 7, 'A' to 6, 'B' to 6,
        'C' to 6, 'D' to 6, 'E' to 6, 'F' to 6, 'G' to 6,
        'H' to 6, 'I' to 4, 'J' to 6, 'K' to 6, 'L' to 6,
        'M' to 6, 'N' to 6, 'O' to 6, 'P' to 6, 'Q' to 6,
        'R' to 6, 'S' to 6, 'T' to 6, 'U' to 6, 'V' to 6,
        'W' to 6, 'X' to 6, 'Y' to 6, 'Z' to 6, '_' to 6,
        'a' to 6, 'b' to 6, 'c' to 6, 'd' to 6, 'e' to 6,
        'f' to 5, 'g' to 6, 'h' to 6, 'i' to 2, 'j' to 6,
        'k' to 5, 'l' to 3, 'm' to 6, 'n' to 6, 'o' to 6,
        'p' to 6, 'q' to 6, 'r' to 5, 's' to 6, 't' to 4,
        'u' to 6, 'v' to 6, 'w' to 6, 'x' to 6, 'y' to 6,
        'z' to 6
    )

    fun getWidth(char: Char): Int {
        return charWidths[char] ?: 6
    }

    fun getStringWidth(text: String): Int {
        return text.sumOf { getWidth(it) + 1 } - 1 // 1 pixel spacing between chars
    }
}

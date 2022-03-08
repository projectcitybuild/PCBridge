package com.projectcitybuild.modules.messaging.serializers

import com.projectcitybuild.modules.messaging.MessageBuilder
import com.projectcitybuild.modules.messaging.components.Color
import com.projectcitybuild.modules.messaging.components.Decoration
import com.projectcitybuild.modules.messaging.tokens.DividerToken
import com.projectcitybuild.modules.messaging.tokens.TextToken
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text as HoverText

class TextComponentSerializer {
    private val maxLineLength = 53  // Hardcoded in Minecraft client
    private val linebreak = "\n"

    fun serialize(builder: MessageBuilder): TextComponent {
        return builder.tokens.withIndex().fold(TextComponent()) { textComponent, token ->
            val value = token.value

            when (value) {
                is TextToken -> {
                    value.parts.forEach { part ->
                        when (part) {
                            is TextToken.Part.Regular -> TextComponent()
                                .apply {
                                    text = part.text
                                    color = part.color.toChatColor()
                                    isItalic = part.isItalic
                                    isBold = part.isBold
                                    isStrikethrough = part.isStrikethrough
                                }
                                .let { originalTextComponent ->
                                    val decoration = part.decoration
                                        ?: return originalTextComponent

                                    // Decorations are also colors in Bungeecord/Spigot, so we need
                                    // an additional TextComponent as only one color can be assigned
                                    // per TextComponent
                                    return TextComponent()
                                        .apply { color = decoration.toChatColor() }
                                        .add(originalTextComponent)
                                }

                            is TextToken.Part.URL -> textComponent.add(TextComponent().apply {
                                text = part.string
                                color = ChatColor.WHITE
                                isUnderlined = true
                                clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, part.string)
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, HoverText(part.string))
                            })

                            is TextToken.Part.Command -> textComponent.add(TextComponent().apply {
                                text = part.text
                                color = ChatColor.WHITE
                                isBold = true
                                isUnderlined = true
                                clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, part.command)
                                hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, HoverText("/" + part.command))
                            })
                        }
                    }
                }
                is DividerToken -> {
                    textComponent.add(
                        TextComponent("-".repeat(maxLineLength))
                    )
                }
            }

            val isLastLine = token.index == builder.tokens.size - 1
            if (!isLastLine) {
                textComponent.add(linebreak)
            }

            return textComponent
        }
    }
}

private fun Color.toChatColor(): ChatColor {
    return when (this) {
        Color.BLACK -> ChatColor.BLACK
        Color.DARK_BLUE -> ChatColor.DARK_BLUE
        Color.DARK_GREEN -> ChatColor.DARK_GREEN
        Color.DARK_AQUA -> ChatColor.DARK_AQUA
        Color.DARK_RED -> ChatColor.DARK_RED
        Color.DARK_PURPLE -> ChatColor.DARK_PURPLE
        Color.GOLD -> ChatColor.GOLD
        Color.GRAY -> ChatColor.GRAY
        Color.DARK_GRAY -> ChatColor.DARK_GRAY
        Color.BLUE -> ChatColor.BLUE
        Color.GREEN -> ChatColor.GREEN
        Color.AQUA -> ChatColor.AQUA
        Color.RED -> ChatColor.RED
        Color.LIGHT_PURPLE -> ChatColor.LIGHT_PURPLE
        Color.YELLOW -> ChatColor.YELLOW
        Color.WHITE -> ChatColor.WHITE
    }
}

private fun Decoration.toChatColor(): ChatColor {
    return when (this) {
        Decoration.MAGIC -> ChatColor.MAGIC
        Decoration.BOLD -> ChatColor.BOLD
        Decoration.STRIKETHROUGH -> ChatColor.STRIKETHROUGH
        Decoration.UNDERLINE -> ChatColor.UNDERLINE
        Decoration.ITALIC -> ChatColor.ITALIC
        Decoration.RESET -> ChatColor.RESET
    }
}
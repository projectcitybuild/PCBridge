package com.projectcitybuild.modules.messaging.serializers

import com.projectcitybuild.modules.messaging.MessageBuilder
import com.projectcitybuild.modules.messaging.components.Color
import com.projectcitybuild.modules.messaging.components.Decoration
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent

class TextComponentSerializer {
    fun serialize(builder: MessageBuilder): TextComponent {
        
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
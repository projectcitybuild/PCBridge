package com.projectcitybuild.platforms.spigot.environment

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent

class MessageFactory {

    fun error(message: String): TextComponent {
        return TextComponent()
            .also {
                it.addExtra(
                    TextComponent("[❌ ERROR]").apply {
                        it.color = ChatColor.RED
                        it.isBold = true
                    }
                )
                it.addExtra(
                    TextComponent(" $message").apply {
                        it.color = ChatColor.RED
                    }
                )
            }
    }
}

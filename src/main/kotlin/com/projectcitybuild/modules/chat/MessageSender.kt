package com.projectcitybuild.modules.chat

import com.projectcitybuild.core.contracts.ChatMessageReceiver
import com.projectcitybuild.platforms.spigot.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent

class MessageSender(
    private val receiver: ChatMessageReceiver
) {
    fun error(message: String) {
        receiver.sendMessage(
            TextComponent()
                .add("Error") { it.color = ChatColor.RED }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun success(message: String) {
        receiver.sendMessage(
            TextComponent()
                .add("Success") { it.color = ChatColor.GREEN }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun info(message: String) {
        receiver.sendMessage(
            TextComponent()
                .add("Info") { it.color = ChatColor.GRAY }
                .add(" » ") { it.color = ChatColor.GOLD }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }
}
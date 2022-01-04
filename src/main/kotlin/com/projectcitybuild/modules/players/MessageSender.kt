package com.projectcitybuild.modules.players

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
                .add("▐ Error: ") { it.color = ChatColor.RED }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun success(message: String) {
        receiver.sendMessage(
            TextComponent()
                .add("▐ ") { it.color = ChatColor.GREEN }
                .add(message) { it.color = ChatColor.WHITE }
        )
    }

    fun info(message: String, isMultiLine: Boolean = false) {
        val messagesByLine = if (isMultiLine) message.split("\n") else listOf(message)

        val tc = TextComponent()
        messagesByLine.forEachIndexed { index, message ->
            tc
                .add("▐ ") { it.color = ChatColor.GRAY }
                .add(message) { it.color = ChatColor.WHITE }

            if (index < messagesByLine.count()) {
                tc.add("\n")
            }
        }

        receiver.sendMessage(tc)
    }
}
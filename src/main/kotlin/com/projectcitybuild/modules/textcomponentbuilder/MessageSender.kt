package com.projectcitybuild.modules.textcomponentbuilder

import com.projectcitybuild.platforms.bungeecord.extensions.add
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
        val messagesByLineBreak = if (isMultiLine) message.split("\n") else listOf(message)

        // Ensure that strings that wrap onto the next line also have the
        // symbols at the start
        val maxLineLength = 53
        val startSymbolLength = 3
        val messagesByLine = messagesByLineBreak.flatMap { it.chunked(maxLineLength - startSymbolLength) }

        val tc = TextComponent()
        messagesByLine.forEachIndexed { index, string ->
            tc
                .add("▐ ") { it.color = ChatColor.GRAY }
                .add(string) { it.color = ChatColor.WHITE }

            if (index < messagesByLine.count()) {
                tc.add("\n")
            }
        }

        receiver.sendMessage(tc)
    }

    fun action(message: String) {
        receiver.sendMessage(
            TextComponent(message).also {
                it.color = ChatColor.GRAY
                it.isItalic = true
            }
        )
    }
}
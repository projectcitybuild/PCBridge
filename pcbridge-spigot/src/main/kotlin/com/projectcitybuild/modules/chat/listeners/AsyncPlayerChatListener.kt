package com.projectcitybuild.modules.chat.listeners

import com.projectcitybuild.modules.chat.ChatBadgeFormatter
import com.projectcitybuild.modules.chat.ChatGroupFormatter
import com.projectcitybuild.support.textcomponent.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.player.AsyncPlayerChatEvent

// TODO: split this up and move it to appropriate modules
class AsyncPlayerChatListener(
    private val server: Server,
    private val chatGroupFormatter: ChatGroupFormatter,
    private val chatBadgeFormatter: ChatBadgeFormatter,
) {
    fun handle(event: AsyncPlayerChatEvent) {
        // Semi-dangerous workaround for other plugins not receiving chat.
        //
        // The typical way to format chat would be to modify the message in the event,
        // however that doesn't support HoverText, which we need because all our groups
        // are abbreviated and similar-looking.
        //
        // We can't cancel the chat event either, as that would prevent other plugins
        // like DiscordSRV from receiving the message and sending it to Discord. This is a
        // hack that lets the original message be sent (and seen by other plugins), but
        // will essentially be cancelled for any online players.
        event.recipients.clear()

        val format = chatGroupFormatter.get(playerUUID = event.player.uniqueId)
        val badges = chatBadgeFormatter.get(playerUUID = event.player.uniqueId)

        val tc = TextComponent()
            .add(badges)
            .add(format.prefix)
            .add(format.groups)
            .add(" ") { it.color = ChatColor.RESET }
            .add(TextComponent.fromLegacyText(event.player.displayName))
            .add(format.suffix)
            .add(": ") { it.color = ChatColor.RESET }
            .add(TextComponent.fromLegacyText(event.message))

        server.onlinePlayers.forEach { it.spigot().sendMessage(tc) }
    }
}

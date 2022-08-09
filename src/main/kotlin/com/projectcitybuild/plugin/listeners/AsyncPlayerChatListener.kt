package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.textcomponentbuilder.add
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import javax.inject.Inject

class AsyncPlayerChatListener @Inject constructor(
    private val server: Server,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatGroupFormatter: ChatGroupFormatter
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        val senderConfig = playerConfigRepository.get(event.player.uniqueId)!!
        if (senderConfig.isMuted) {
            event.player.send().error("You cannot talk while muted")
            event.isCancelled = true
            return
        }

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

        val test = TextComponent("★").also {
            it.color = ChatColor.GOLD
            it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(
                "${ChatColor.GOLD}★ ${ChatColor.WHITE}Blockbuster Build-Off Winner\n" +
                "${ChatColor.GOLD}❈ ${ChatColor.WHITE}Blockbuster Build-Off Winner"
            ))
        }

        val tc = TextComponent()
            .add(test)
            .add(" ")
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

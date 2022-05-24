package com.projectcitybuild.plugin.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.features.chat.ChatGroupFormatter
import com.projectcitybuild.modules.textcomponentbuilder.add
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import javax.inject.Inject

class ChatListener @Inject constructor(
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

        // Super unsafe, but no other option as cancelling the event (as per the
        // normal way) will interfere with a lot of other plugins
        event.recipients.clear()

        val format = chatGroupFormatter.format(event.player)

        val tc = TextComponent()
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

package com.projectcitybuild.features.chat.listeners

import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin

class ChatListener(
    private val plugin: Plugin
): Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        MessageToBungeecord(
            plugin,
            event.player,
            SubChannel.GLOBAL_CHAT,
            arrayOf(
                event.message,
                event.player.displayName,
            )
        ).send()

        // Super unsafe, but no other option as cancelling the event (as per the
        // normal way) will interfere with a lot of other plugins
        event.recipients.clear()
    }
}
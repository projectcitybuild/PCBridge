package com.projectcitybuild.features.chat.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.spigot.MessageToBungeecord
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin
import javax.inject.Inject

class ChatListener @Inject constructor(
    private val plugin: Plugin
): SpigotListener {

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
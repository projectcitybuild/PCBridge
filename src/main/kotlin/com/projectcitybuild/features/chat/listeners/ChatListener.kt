package com.projectcitybuild.features.chat.listeners

import com.projectcitybuild.core.SpigotListener
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.ProxyMessenger
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import javax.inject.Inject

class ChatListener @Inject constructor(
    private val proxyMessenger: ProxyMessenger,
) : SpigotListener {

    @EventHandler(priority = EventPriority.HIGHEST)
    fun onAsyncPlayerChatEvent(event: AsyncPlayerChatEvent) {
        proxyMessenger.sendToProxy(
            sender = event.player,
            subChannel = SubChannel.GLOBAL_CHAT,
            params = arrayOf(
                event.message,
                event.player.displayName,
            )
        )

        // Super unsafe, but no other option as cancelling the event (as per the
        // normal way) will interfere with a lot of other plugins
        event.recipients.clear()
    }
}

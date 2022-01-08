package com.projectcitybuild.platforms.spigot.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
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
        val out = ByteStreams.newDataOutput()
        out.writeUTF(SubChannel.GLOBAL_CHAT)
        out.writeUTF(event.message)

        event.player.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())

        // Super unsafe, but no other option as cancelling the event (as per the
        // normal way) will interfere with a lot of other plugins
        event.recipients.clear()
    }
}
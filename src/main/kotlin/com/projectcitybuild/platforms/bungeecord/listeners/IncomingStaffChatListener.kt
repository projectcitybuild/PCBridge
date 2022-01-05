package com.projectcitybuild.platforms.bungeecord.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.platforms.bungeecord.extensions.add
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class IncomingStaffChatListener(
    private val proxy: ProxyServer
): Listener {

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        if (subChannel != SubChannel.STAFF_CHAT)
            return

        val player = event.receiver as ProxiedPlayer
        if (!player.hasPermission("pcbridge.chat.staff_channel.send"))
            return

        val message = stream.readUTF()

        proxy.players.forEach { player ->
            if (player.hasPermission("pcbridge.chat.staff_channel.receive"))
                player.sendMessage(
                    TextComponent()
                        .add("Staff") { it.color = ChatColor.YELLOW }
                        .add(" » ") { it.color = ChatColor.GRAY }
                        .add(message) {
                            it.color = ChatColor.YELLOW
                            it.isItalic = true
                        }
                )
        }
    }
}
package com.projectcitybuild.platforms.bungeecord.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.players.PlayerRepository
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class IncomingChatListener(
    private val proxy: ProxyServer,
    private val playerRepository: PlayerRepository
): Listener {

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        if (subChannel != SubChannel.GLOBAL_CHAT)
            return

        val sender = event.receiver
        if (sender is ProxiedPlayer) {
            val cachedPlayer = playerRepository.get(sender.uniqueId)
            if (cachedPlayer.isMuted) {
                sender.send().error("You cannot talk while muted")
                return
            }
        }

        val message = stream.readUTF()

        proxy.players.forEach {
            it.sendMessage(TextComponent(message))
        }
    }
}
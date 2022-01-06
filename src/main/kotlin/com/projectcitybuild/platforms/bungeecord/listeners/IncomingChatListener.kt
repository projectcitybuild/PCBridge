package com.projectcitybuild.platforms.bungeecord.listeners

import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.PluginMessageEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler

class IncomingChatListener(
    private val proxy: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): Listener {

    @EventHandler
    fun onPluginMessageReceived(event: PluginMessageEvent) {
        if (event.tag != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(event.data)
        val subChannel = stream.readUTF()

        if (subChannel != SubChannel.GLOBAL_CHAT)
            return

        CoroutineScope(Dispatchers.IO).launch {
            var recipients = proxy.players

            val sender = event.receiver
            if (sender is ProxiedPlayer) {
                val senderConfig = playerConfigRepository.get(sender.uniqueId)
                if (senderConfig.isMuted) {
                    sender.send().error("You cannot talk while muted")
                    return@launch
                }
                recipients = recipients.filter { recipient ->
                    val recipientConfig = playerConfigRepository.get(recipient.uniqueId)
                    !recipientConfig.unwrappedChatIgnoreList.contains(sender.uniqueId)
                }
            }

            val message = stream.readUTF()

            recipients.forEach {
                it.sendMessage(TextComponent(message))
            }
        }
    }
}
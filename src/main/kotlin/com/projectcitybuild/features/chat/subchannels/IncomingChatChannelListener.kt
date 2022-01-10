package com.projectcitybuild.features.chat.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.chat.ChatGroupFormatBuilder
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.extensions.add
import com.projectcitybuild.modules.textcomponentbuilder.send
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer

class IncomingChatChannelListener(
    private val proxy: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatGroupFormatBuilder: ChatGroupFormatBuilder
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.GLOBAL_CHAT

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        val message = stream.readUTF()
        val senderDisplayName = stream.readUTF()

        val player = receiver as? ProxiedPlayer
            ?: return

        CoroutineScope(Dispatchers.IO).launch {
            var recipients = proxy.players

            val senderConfig = playerConfigRepository.get(player.uniqueId)
            if (senderConfig.isMuted) {
                player.send().error("You cannot talk while muted")
                return@launch
            }
            recipients = recipients.filter { recipient ->
                val recipientConfig = playerConfigRepository.get(recipient.uniqueId)
                !recipientConfig.unwrappedChatIgnoreList.contains(player.uniqueId)
            }

            // TODO: stop IO thrashing and cache all this instead
            val format = chatGroupFormatBuilder.format(player)

            val tc = TextComponent()
                .add(format.prefix)
                .add(format.groups)
                .add(" ") { it.color = ChatColor.RESET }
                .add(TextComponent.fromLegacyText(senderDisplayName))
                .add(format.suffix)
                .add(": ") { it.color = ChatColor.RESET }
                .add(TextComponent.fromLegacyText(message))

            recipients.forEach { it.sendMessage(tc) }
        }
    }
}
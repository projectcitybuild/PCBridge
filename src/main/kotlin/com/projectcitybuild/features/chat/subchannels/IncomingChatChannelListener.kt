package com.projectcitybuild.features.chat.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.chat.ChatGroupFormatBuilder
import com.projectcitybuild.features.chat.repositories.ChatIgnoreRepository
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.extensions.add
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import javax.inject.Inject

class IncomingChatChannelListener @Inject constructor(
    private val proxy: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val chatGroupFormatBuilder: ChatGroupFormatBuilder
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.GLOBAL_CHAT

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        val message = stream.readUTF()
        val senderDisplayName = stream.readUTF()

        val player = receiver as? ProxiedPlayer
            ?: return

        CoroutineScope(Dispatchers.IO).launch {
            val senderConfig = playerConfigRepository.get(player.uniqueId)!!
            if (senderConfig.isMuted) {
                player.send().error("You cannot talk while muted")
                return@launch
            }

            val ignorers = chatIgnoreRepository.ignorerIds(senderConfig.id)
            val recipients =
                if (ignorers.isEmpty()) { proxy.players }
                else {
                    proxy.players.filter { recipient ->
                        val recipientConfig = playerConfigRepository.get(recipient.uniqueId)
                        !ignorers.contains(recipientConfig!!.id)
                    }
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
package com.projectcitybuild.features.afk.listeners

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer

class IncomingAFKEndListener(
    private val proxy: ProxyServer,
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.AFK_END

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        val player = sender as ProxiedPlayer

        proxy.broadcast(
            TextComponent("${player.name} is no longer AFK").also {
                it.color = ChatColor.GRAY
                it.isItalic = true
            }
        )
    }
}
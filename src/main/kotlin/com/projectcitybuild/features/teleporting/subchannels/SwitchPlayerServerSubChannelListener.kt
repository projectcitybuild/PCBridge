package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.bungeecord.BungeecordSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.connection.Connection
import net.md_5.bungee.api.connection.ProxiedPlayer
import java.util.*
import javax.inject.Inject

class SwitchPlayerServerSubChannelListener @Inject constructor(
    private val proxyServer: ProxyServer,
    private val logger: PlatformLogger,
): BungeecordSubChannelListener {

    override val subChannel = SubChannel.SWITCH_PLAYER_SERVER

    override fun onBungeecordReceivedMessage(receiver: Connection, sender: Connection, stream: ByteArrayDataInput) {
        if (receiver !is ProxiedPlayer)
            return

        val targetPlayerUUID = UUID.fromString(stream.readUTF())
        val targetPlayer = proxyServer.getPlayer(targetPlayerUUID)
        if (targetPlayer == null) {
            logger.warning("Could not find proxy player $targetPlayerUUID to switch. Did they disconnect?")
            return
        }

        val destinationServerName = stream.readUTF()
        val destinationServer = proxyServer.getServerInfo(destinationServerName)
        if (destinationServer == null) {
            logger.warning("Could not find destination server $destinationServerName for player switch")
            return
        }

        targetPlayer.connect(destinationServer)
    }
}
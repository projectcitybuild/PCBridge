package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.teleporting.events.PlayerPreTeleportEvent
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import javax.inject.Inject

class AcrossServerTeleportChannelListener @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger
): SpigotSubChannelListener {

    override val subChannel = SubChannel.TP_ACROSS_SERVER

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
        val targetPlayerUUID = UUID.fromString(stream.readUTF())
        val destinationServerName = stream.readUTF()

        val targetPlayer = plugin.server.getPlayer(targetPlayerUUID)
        if (targetPlayer == null) {
            logger.warning("Could not find player matching uuid $targetPlayerUUID. Did they disconnect?")
            return
        }

        Bukkit.getPluginManager().callEvent(
            PlayerPreTeleportEvent(targetPlayer, targetPlayer.location)
        )

        val out = ByteStreams.newDataOutput()
        out.writeUTF("Connect")
        out.writeUTF(destinationServerName)
        targetPlayer.sendPluginMessage(plugin, Channel.BUNGEECORD, out.toByteArray())
    }
}
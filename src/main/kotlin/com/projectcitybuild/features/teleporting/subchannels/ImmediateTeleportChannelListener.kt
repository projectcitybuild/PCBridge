package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import javax.inject.Inject

class ImmediateTeleportChannelListener @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger
): SpigotSubChannelListener {

    override val subChannel = SubChannel.TP_IMMEDIATELY

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
        val targetPlayerUUID = UUID.fromString(stream.readUTF())
        val targetPlayer = plugin.server.getPlayer(targetPlayerUUID)
        if (targetPlayer == null) {
            logger.warning("Could not find player. Did they disconnect?")
            return
        }

        val destinationPlayerUUID = UUID.fromString(stream.readUTF())
        val destinationPlayer = plugin.server.getPlayer(destinationPlayerUUID)
        if (destinationPlayer == null) {
            logger.warning("Could not find destination player. Did they disconnect?")
            return
        }

        logger.debug("Immediately teleporting $targetPlayerUUID to location of $destinationPlayerUUID")

        targetPlayer.teleport(destinationPlayer.location)
    }
}
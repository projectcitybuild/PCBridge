package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
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
        val teleportingPlayerUUID = UUID.fromString(stream.readUTF())
        val teleportTargetPlayerUUID = UUID.fromString(stream.readUTF())

        val teleportingPlayer = plugin.server.getPlayer(teleportingPlayerUUID)
        if (teleportingPlayer == null) {
            logger.warning("Attempted to teleport, but could not find the command sender")
            return
        }

        val teleportTargetPlayer = plugin.server.getPlayer(teleportTargetPlayerUUID)
        if (teleportTargetPlayer == null) {
            teleportingPlayer.send().error("Could not find target player. Did they disconnect?")
            return
        }

        teleportingPlayer.teleport(teleportTargetPlayer)
        teleportingPlayer.send().action("Teleported to ${teleportTargetPlayer.name}")
    }
}
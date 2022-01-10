package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.modules.channels.SubChannelListener
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class ImmediateTeleportChannelListener(
    private val plugin: Plugin,
    private val logger: LoggerProvider
): SubChannelListener {

    override fun onSpigotMessageReceived(player: Player?, stream: ByteArrayDataInput) {
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
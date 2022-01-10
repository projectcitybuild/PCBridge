package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.modules.channel.SubChannelListener
import com.projectcitybuild.modules.logger.LoggerProvider
import com.projectcitybuild.modules.sessioncache.SessionCache
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class AwaitJoinTeleportChannelListener(
    private val plugin: Plugin,
    private val logger: LoggerProvider,
    private val sessionCache: SessionCache
): SubChannelListener {

    override fun onSpigotMessageReceived(player: Player?, stream: ByteArrayDataInput) {
        val teleportingPlayerUUID = UUID.fromString(stream.readUTF())
        val teleportTargetPlayerUUID = UUID.fromString(stream.readUTF())

        logger.debug("Queuing teleport for $teleportingPlayerUUID to location of $teleportTargetPlayerUUID")

        sessionCache.pendingJoinActions[teleportingPlayerUUID] = { _, event ->
            val targetPlayer = plugin.server.getPlayer(teleportTargetPlayerUUID)
            if (targetPlayer == null) {
                event.player.send().error("Could not find target player for teleport")
            } else {
                event.spawnLocation = targetPlayer.location
            }
        }
    }
}
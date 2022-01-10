package com.projectcitybuild.platforms.spigot.listeners

import com.google.common.io.ByteArrayDataInput
import com.google.common.io.ByteStreams
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.entities.Channel
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.old_modules.sessioncache.PendingJoinAction
import com.projectcitybuild.old_modules.sessioncache.SessionCache
import com.projectcitybuild.platforms.spigot.environment.send
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.messaging.PluginMessageListener
import java.util.*

class IncomingPluginMessageListener(
    private val plugin: Plugin,
    private val sessionCache: SessionCache,
    private val logger: LoggerProvider
): PluginMessageListener {

    override fun onPluginMessageReceived(channel: String?, player: Player?, message: ByteArray?) {
        if (channel != Channel.BUNGEECORD) return

        val stream = ByteStreams.newDataInput(message)
        val subChannel = stream.readUTF()

        when (subChannel) {
            SubChannel.WARP_IMMEDIATELY,
            SubChannel.WARP_AWAIT_JOIN -> onWarp(stream, subChannel)

            SubChannel.TP_IMMEDIATELY,
            SubChannel.TP_AWAIT_JOIN -> onTeleport(stream, subChannel)
        }
    }

    private fun onWarp(stream: ByteArrayDataInput, subChannel: String) {
        val playerUUID = UUID.fromString(stream.readUTF())
        val worldName = stream.readUTF()
        val x = stream.readDouble()
        val y = stream.readDouble()
        val z = stream.readDouble()
        val pitch = stream.readFloat()
        val yaw = stream.readFloat()

        val world = plugin.server.getWorld(worldName)
        if (world == null) {
            logger.warning("Could not find world matching name [$worldName] for warp")
            return
        }
        val location = Location(world, x, y, z, yaw, pitch)

        when (subChannel) {
            SubChannel.WARP_IMMEDIATELY -> {
                logger.debug("Immediately warping $playerUUID to $location")

                val player = plugin.server.getPlayer(playerUUID)
                if (player == null) {
                    logger.warning("Attempted to warp, but could not find player matching UUID [$playerUUID]")
                    return
                }
                player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND)
            }
            SubChannel.WARP_AWAIT_JOIN -> {
                logger.debug("Queuing warp for $playerUUID to $location")

                sessionCache.pendingJoinActions[playerUUID] = PendingJoinAction.TeleportToLocation(location)
            }
        }
    }

    private fun onTeleport(stream: ByteArrayDataInput, subChannel: String) {
        val teleportingPlayerUUID = UUID.fromString(stream.readUTF())
        val teleportTargetPlayerUUID = UUID.fromString(stream.readUTF())

        when (subChannel) {
            SubChannel.TP_IMMEDIATELY -> {
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
                teleportingPlayer.send().success("Teleported to ${teleportTargetPlayer.name}")
            }

            SubChannel.TP_AWAIT_JOIN -> {
                logger.debug("Queuing teleport for $teleportingPlayerUUID to location of $teleportTargetPlayerUUID")

                sessionCache.pendingJoinActions[teleportingPlayerUUID] = PendingJoinAction.TeleportToPlayer(teleportTargetPlayerUUID)
            }
        }
    }
}
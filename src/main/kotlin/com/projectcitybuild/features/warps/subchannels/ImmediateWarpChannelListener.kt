package com.projectcitybuild.features.warps.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.LoggerProvider
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.plugin.Plugin
import java.util.*
import javax.inject.Inject

class ImmediateWarpChannelListener @Inject constructor(
    private val plugin: Plugin,
    private val logger: LoggerProvider
): SpigotSubChannelListener {

    override val subChannel = SubChannel.WARP_IMMEDIATELY

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
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

        logger.debug("Immediately warping $playerUUID to $location")

        val player = plugin.server.getPlayer(playerUUID)
        if (player == null) {
            logger.warning("Attempted to warp, but could not find player matching UUID [$playerUUID]")
            return
        }
        player.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND)
    }
}
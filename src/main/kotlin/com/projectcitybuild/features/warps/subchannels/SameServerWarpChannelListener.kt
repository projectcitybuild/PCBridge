package com.projectcitybuild.features.warps.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.warps.events.PlayerPreWarpEvent
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import javax.inject.Inject

class SameServerWarpChannelListener @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
): SpigotSubChannelListener {

    override val subChannel = SubChannel.WARP_SAME_SERVER

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
        val warpName = stream.readUTF()
        val playerUUID = UUID.fromString(stream.readUTF())
        val worldName = stream.readUTF()
        val x = stream.readDouble()
        val y = stream.readDouble()
        val z = stream.readDouble()
        val pitch = stream.readFloat()
        val yaw = stream.readFloat()

        val targetPlayer = plugin.server.getPlayer(playerUUID)
        if (targetPlayer == null) {
            logger.warning("Could not find player to warp. Did they disconnect?")
            return
        }

        Bukkit.getPluginManager().callEvent(
            PlayerPreWarpEvent(targetPlayer, targetPlayer.location)
        )

        val world = plugin.server.getWorld(worldName)
        if (world == null) {
            logger.warning("Could not find world matching name [$worldName] for warp")
            player?.send()?.error("Cannot find world $worldName")
            return
        }
        val location = Location(world, x, y, z, yaw, pitch)

        logger.debug("(Same server) Warping player $playerUUID to $location")

        targetPlayer.teleport(location)

        targetPlayer.send().action("Warped to $warpName")
    }
}
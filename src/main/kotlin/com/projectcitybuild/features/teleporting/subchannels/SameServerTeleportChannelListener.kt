package com.projectcitybuild.features.teleporting.subchannels

import com.google.common.io.ByteArrayDataInput
import com.projectcitybuild.entities.SubChannel
import com.projectcitybuild.features.teleporting.events.PlayerPreSummonEvent
import com.projectcitybuild.modules.channels.spigot.SpigotSubChannelListener
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import javax.inject.Inject

class SameServerTeleportChannelListener @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger
): SpigotSubChannelListener {

    override val subChannel = SubChannel.TP_SAME_SERVER

    override fun onSpigotReceivedMessage(player: Player?, stream: ByteArrayDataInput) {
        val targetPlayerUUID = UUID.fromString(stream.readUTF())
        val targetPlayer = plugin.server.getPlayer(targetPlayerUUID)
        if (targetPlayer == null) {
            logger.warning("Could not find player. Did they disconnect?")
            return
        }

        Bukkit.getPluginManager().callEvent(
            PlayerPreSummonEvent(targetPlayer, targetPlayer.location)
        )

        val destinationPlayerUUID = UUID.fromString(stream.readUTF())
        val destinationPlayer = plugin.server.getPlayer(destinationPlayerUUID)
        if (destinationPlayer == null) {
            logger.warning("Could not find destination player. Did they disconnect?")
            return
        }

        logger.debug("Immediately teleporting $targetPlayerUUID to location of $destinationPlayerUUID")

        targetPlayer.teleport(destinationPlayer.location)

        val isSummon = stream.readBoolean()
        if (isSummon) {
            destinationPlayer.send().action("You summoned ${targetPlayer.name} to you")
            targetPlayer.send().action("You were summoned to ${destinationPlayer.name}")

        } else {
            destinationPlayer.send().action("${targetPlayer.name} teleported to you")
            targetPlayer.send().action("Teleported to ${destinationPlayer.name}")
        }
    }
}
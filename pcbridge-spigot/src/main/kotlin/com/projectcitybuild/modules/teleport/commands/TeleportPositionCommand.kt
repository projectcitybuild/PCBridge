package com.projectcitybuild.modules.teleport.commands

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerTeleportEvent

class TeleportPositionCommand {

    fun execute(
        commandSender: Player,
        x: Double,
        y: Double,
        z: Double,
        yaw: Float?,
        pitch: Float?,
        world: World?,
    ) {
        val targetWorld = world ?: commandSender.world
        val location = Location(
            targetWorld,
            x,
            y,
            z,
            (yaw ?: 0f).coerceIn(-180f, 180f),
            (pitch ?: 0f).coerceIn(-90f, 90f),
        )
        commandSender.teleport(location, PlayerTeleportEvent.TeleportCause.COMMAND)
        commandSender.spigot().sendMessage(
            TextComponent("Teleporting to {x: $x, y: $y, z: $z, world: ${targetWorld.name}}").apply {
                color = ChatColor.GRAY
                isItalic = true
            }
        )
    }
}
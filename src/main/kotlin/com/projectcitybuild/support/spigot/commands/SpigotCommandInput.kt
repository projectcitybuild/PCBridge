package com.projectcitybuild.support.spigot.commands

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

data class SpigotCommandInput(
    val sender: CommandSender,
    val args: List<String>,
    val isConsole: Boolean
) {
    val player: Player
        get() = sender as Player
}

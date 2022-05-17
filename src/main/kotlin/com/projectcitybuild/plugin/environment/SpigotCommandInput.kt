package com.projectcitybuild.plugin.environment

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

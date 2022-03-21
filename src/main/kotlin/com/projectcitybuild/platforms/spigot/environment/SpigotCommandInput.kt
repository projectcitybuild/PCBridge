package com.projectcitybuild.platforms.spigot.environment

import org.bukkit.command.CommandSender

data class SpigotCommandInput(
    val sender: CommandSender,
    val args: List<String>,
    val isConsole: Boolean
)

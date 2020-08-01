package com.projectcitybuild.entities

import org.bukkit.command.CommandSender

data class CommandInput(
        val sender: CommandSender,
        val args: Array<String>,
        val isConsole: Boolean
)
package com.projectcitybuild.entities

import org.bukkit.command.CommandSender

data class CommandInput(
        val sender: CommandSender,
        val args: List<String>,
        val isConsole: Boolean
) {

    var hasArguments: Boolean = args.isNotEmpty()
}
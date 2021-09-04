package com.projectcitybuild.core.entities

import org.bukkit.command.CommandSender

data class CommandInput(
        val sender: CommandSender,
        val args: Array<out String>,
        val isConsole: Boolean
) {

    var hasArguments: Boolean = !args.isEmpty()
}
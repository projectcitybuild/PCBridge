package com.projectcitybuild

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class WarpsCommand: SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        println("Test")
        return true
    }
}
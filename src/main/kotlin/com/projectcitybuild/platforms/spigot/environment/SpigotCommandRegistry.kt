package com.projectcitybuild.platforms.spigot.environment

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import com.projectcitybuild.core.contracts.*
import com.projectcitybuild.entities.CommandInput
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommandRegistry constructor(
        private val plugin: JavaPlugin,
        private val logger: LoggerProvider
) {
    fun register(command: Commandable) {
        val aliases = command.aliases.plus(command.label)

        aliases.forEach { alias ->
            class BridgedCommand(private val wrappedCommand: Commandable): SuspendingCommandExecutor {
                override suspend fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
                    return try {
                        val input = CommandInput(
                                sender = sender,
                                args = args.toList(),
                                isConsole = sender !is Player
                        )
                        when (wrappedCommand.execute(input)) {
                            CommandResult.EXECUTED -> true
                            CommandResult.INVALID_INPUT -> false
                        }
                    }
                    catch (error: Exception) {
                        sender.sendMessage("An internal error occurred performing your command")
                        error.localizedMessage.let { message -> logger.fatal(message) }
                        error.printStackTrace()
                        true
                    }
                }
            }
            plugin.getCommand(alias).let {
                it.setSuspendingExecutor(BridgedCommand(command))
                it.permission = command.permission
            }
        }
    }
}
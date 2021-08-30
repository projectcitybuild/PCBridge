package com.projectcitybuild.platforms.spigot

import com.projectcitybuild.core.contracts.*
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.CommandResult
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class SpigotCommandDelegate constructor(
        private val plugin: JavaPlugin,
        private val logger: LoggerProvider
) {
    fun register(command: Commandable) {
        command.aliases.plus(command.label).forEach { alias ->
            plugin.getCommand(alias)?.setExecutor { sender, _, _, args ->
                try {
                    val input = CommandInput(
                            sender = sender,
                            args = args ?: arrayOf(),
                            isConsole = sender !is Player
                    )
                    when (command.execute(input)) {
                        CommandResult.EXECUTED -> true
                        CommandResult.INVALID_INPUT -> false
                    }
                }
                catch (error: Exception) {
                    sender.sendMessage("An internal error occurred performing your command")
                    error.localizedMessage.let { message -> logger.fatal(message) }
                    true
                }
            }
            plugin.getCommand(alias)?.permission = command.permission
        }
    }
}
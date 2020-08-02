package com.projectcitybuild.spigot

import com.projectcitybuild.core.contracts.CommandDelegatable
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.LogLevel
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

class SpigotCommandDelegate constructor(
        private val plugin: WeakReference<JavaPlugin>,
        private val environment: EnvironmentProvider
): CommandDelegatable {

    override fun register(command: Commandable) {
        command.aliases.plus(command.label).forEach { alias ->
            val plugin = plugin.get() ?: return

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
                    error.localizedMessage.let { message -> environment.log(LogLevel.FATAL, message) }
                    true
                }
            }
            plugin.getCommand(alias)?.permission = command.permission
        }
    }
}
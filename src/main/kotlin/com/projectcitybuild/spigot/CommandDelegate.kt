package com.projectcitybuild.spigot

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

internal class CommandDelegate constructor(val plugin: WeakReference<JavaPlugin>,
                                           val environment: Environment) {

    fun register(command: Commandable) {
        command.inject(environment)

        command.aliases.plus(command.label).forEach { alias ->
            plugin.get()?.getCommand(alias)?.setExecutor { sender, _, _, args ->
                try {
                    command.execute(
                            sender = sender,
                            args = args ?: arrayOf(),
                            isConsole = sender !is Player
                    )
                } catch (error: Exception) {
                    sender.sendMessage("An error occurred performing a command")
                    true
                }
            }
        }
    }
}
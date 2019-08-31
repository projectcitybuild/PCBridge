package com.projectcitybuild.spigot

import com.projectcitybuild.PCBridge
import com.projectcitybuild.core.contracts.CommandDelegatable
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.models.PluginConfig
import io.sentry.event.UserBuilder
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference

internal class SpigotCommandDelegate constructor(
        val plugin: WeakReference<JavaPlugin>,
        val environment: EnvironmentProvider
    ): CommandDelegatable {

    override fun register(command: Commandable) {
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
                    sender.sendMessage("An internal error occurred performing your command")
                    error.localizedMessage.let { message -> environment.log(LogLevel.FATAL, message) }
                    reportError(command, sender, args, error)
                    true
                }
            }
            plugin.get()?.getCommand(alias)?.permission = command.permission
        }
    }

    private fun reportError(command: Commandable, sender: CommandSender, args: Array<String>, error: Exception) {
        val plugin = plugin.get()

        if (plugin is PCBridge) {
            val sentry = plugin.sentry

            val user = UserBuilder()
                    .setId(if(sender is Player) sender.uniqueId.toString() else "console")
                    .build()

            sentry?.context?.user = user
            sentry?.context?.addExtra("command", command.label)
            sentry?.context?.addExtra("args", args)
            sentry?.sendException(error)
        }
    }
}
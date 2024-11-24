package com.projectcitybuild.pcbridge.paper.support.spigot

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.projectcitybuild.pcbridge.paper.core.errors.SentryReporter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.lang.IllegalStateException

@Deprecated("Use Brigadier instead")
class SpigotCommandRegistry(
    private val plugin: JavaPlugin,
    private val sentry: SentryReporter,
) {
    private val registered: MutableSet<String> = mutableSetOf()

    fun <T> register(
        handler: SpigotCommand<T>,
        argsParser: CommandArgsParser<T>,
        tabCompleter: SuspendingTabCompleter? = null,
    ) {
        val label = handler.label
        check(!registered.contains(label)) {
            "$label command already registered"
        }
        val pluginCommand = plugin.getCommand(label)
        checkNotNull(pluginCommand) {
            "$label command is not found. Did you forget to add it to plugin.yml?"
        }
        pluginCommand.setSuspendingExecutor(
            object : SuspendingCommandExecutor {
                override suspend fun onCommand(
                    sender: CommandSender,
                    command: Command,
                    label: String,
                    args: Array<out String>,
                ): Boolean {
                    runCatching {
                        handler.run(
                            sender = sender,
                            args = argsParser.parse(args),
                        )
                    }.onFailure {
                        when (it) {
                            is IllegalStateException -> {
                                sender.sendMessage(
                                    Component.text("Error: ${it.localizedMessage}")
                                        .color(NamedTextColor.RED),
                                )
                            }
                            is BadCommandUsageException -> {
                                handler.displayUsage(sender)
                            }
                            is UnauthorizedCommandException -> {
                                sender.sendMessage(
                                    Component.text("Error: You do not have permission to use this command")
                                        .color(NamedTextColor.RED),
                                )
                            }
                            else -> {
                                sender.sendMessage(
                                    Component.text("Error: Something went wrong")
                                        .color(NamedTextColor.RED),
                                )
                                sentry.report(it)
                                throw it
                            }
                        }
                    }
                    return true
                }
            },
        )
        pluginCommand.setSuspendingTabCompleter(
            object : SuspendingTabCompleter {
                override suspend fun onTabComplete(
                    sender: CommandSender,
                    command: Command,
                    alias: String,
                    args: Array<out String>,
                ): List<String>? = handler.tabComplete(
                    sender,
                    command,
                    alias,
                    args,
                )
            }
        )
        registered.add(label)

        if (tabCompleter != null) {
            // TODO: also wrap this
            pluginCommand.setSuspendingTabCompleter(tabCompleter)
        }
    }

    fun unregisterAll() {
        registered.forEach { label ->
            plugin.getCommand(label)?.apply {
                setExecutor(null)
                tabCompleter = null
            }
        }
        registered.clear()
    }
}

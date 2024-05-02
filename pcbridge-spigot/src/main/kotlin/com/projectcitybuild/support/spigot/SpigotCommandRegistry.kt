package com.projectcitybuild.support.spigot

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.projectcitybuild.core.errors.SentryReporter
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.lang.IllegalStateException

class SpigotCommandRegistry(
    private val plugin: JavaPlugin,
    private val audiences: BukkitAudiences,
    private val sentry: SentryReporter,
) {
    private val registered: MutableSet<String> = mutableSetOf()

    fun <T> register(
        handler: SpigotCommand<T>,
        argsParser: CommandArgsParser<T>,
        tabCompleter: SuspendingTabCompleter? = null,
    ) {
        val label = handler.label
        check (!registered.contains(label)) {
            "$label command already registered"
        }
        val pluginCommand = plugin.getCommand(label)
        checkNotNull(pluginCommand) {
            "$label command is not found. Did you forget to add it to plugin.yml?"
        }
        pluginCommand.setSuspendingExecutor(object : SuspendingCommandExecutor {
            override suspend fun onCommand(
                sender: CommandSender,
                command: Command,
                label: String,
                args: Array<out String>
            ): Boolean {
                runCatching {
                    val parsedArgs = argsParser.tryParse(args)
                    if (parsedArgs == null) {
                        handler.displayUsage(sender, audiences)
                    } else {
                        handler.run(
                            sender = sender,
                            args = parsedArgs,
                        )
                    }
                }.onFailure {
                    if (it is IllegalStateException) {
                        val message = Component.text("Error: ${it.localizedMessage}")
                            .color(NamedTextColor.RED)

                        audiences.sender(sender).sendMessage(message)
                    } else {
                        val message = Component.text("Error: Something went wrong")
                            .color(NamedTextColor.RED)

                        audiences.sender(sender).sendMessage(message)
                        sentry.report(it)
                        throw it
                    }
                }
                return true
            }
        })
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
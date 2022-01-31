package com.projectcitybuild.platforms.spigot.environment

import com.github.shynixn.mccoroutine.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.setSuspendingExecutor
import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import dagger.Reusable
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import javax.inject.Inject

@Reusable
class SpigotCommandRegistry @Inject constructor(
    private val plugin: JavaPlugin,
    private val logger: PlatformLogger,
    private val errorReporter: ErrorReporter,
) {
    fun register(spigotCommand: SpigotCommand) {
        val aliases = spigotCommand.aliases.plus(spigotCommand.label)

        aliases.forEach { alias ->
            class BridgedCommand(private val wrappedCommand: SpigotCommand): SuspendingCommandExecutor, TabCompleter {
                override suspend fun onCommand(
                    sender: CommandSender,
                    command: Command,
                    label: String,
                    args: Array<out String>
                ): Boolean {
                    return try {
                        val input = SpigotCommandInput(
                            sender = sender,
                            args = args.toList(),
                            isConsole = sender !is Player
                        )
                        wrappedCommand.execute(input)
                        true
                    } catch (error: InvalidCommandArgumentsException) {
                        sender.spigot().sendMessage(
                            TextComponent(spigotCommand.usageHelp).also {
                                it.color = ChatColor.GRAY
                                it.isItalic = true
                            }
                        )
                        true
                    } catch (error: Exception) {
                        sender.sendMessage("An internal error occurred performing your command")
                        error.localizedMessage.let { message -> logger.fatal(message) }
                        error.printStackTrace()
                        errorReporter.report(error)
                        true
                    }
                }

                override fun onTabComplete(
                    sender: CommandSender?,
                    command: Command?,
                    alias: String?,
                    args: Array<out String>?
                ): MutableList<String> {
                    val improvedArgs = args?.filter { it.isNotEmpty() } ?: emptyList()

                    val list = wrappedCommand.onTabComplete(sender, improvedArgs) ?: emptyList()
                    return list.toMutableList()
                }
            }
            plugin.getCommand(alias).apply {
                val command = BridgedCommand(spigotCommand)
                setSuspendingExecutor(command)
                tabCompleter = command
                permission = spigotCommand.permission
            }
        }
    }
}
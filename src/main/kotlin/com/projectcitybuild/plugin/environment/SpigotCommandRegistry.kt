package com.projectcitybuild.plugin.environment

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
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
        logger.verbose("Registering command: ${spigotCommand::class.java.simpleName}")

        val aliases = spigotCommand.aliases.plus(spigotCommand.label)

        aliases.forEach { alias ->
            class BridgedCommand(private val wrappedCommand: SpigotCommand) : SuspendingCommandExecutor, TabCompleter {
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
                    } catch (error: CannotInvokeFromConsoleException) {
                        sender.send().error("Console cannot use this command")
                        true
                    } catch (error: Exception) {
                        sender.send().error("An internal error occurred performing your command")
                        error.localizedMessage?.let { message -> logger.fatal(message) }
                        error.printStackTrace()
                        errorReporter.report(error)
                        true
                    }
                }

                override fun onTabComplete(
                    p0: CommandSender,
                    p1: Command,
                    p2: String,
                    p3: Array<out String>
                ): MutableList<String>? {
                    val improvedArgs = p3.filter { it.isNotEmpty() }

                    val list = wrappedCommand.onTabComplete(p0, improvedArgs) ?: emptyList()
                    return list.toMutableList()
                }
            }
            plugin.getCommand(alias).let {
                if (it == null) {
                    logger.fatal("Missing $alias command in plugin.yml file")
                    return@let
                }
                val command = BridgedCommand(spigotCommand)
                it.setSuspendingExecutor(command)
                it.tabCompleter = command
                it.permission = spigotCommand.permission
            }
        }
    }
}

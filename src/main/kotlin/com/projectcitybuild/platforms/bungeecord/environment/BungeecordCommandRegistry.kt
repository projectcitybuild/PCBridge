package com.projectcitybuild.platforms.bungeecord.environment

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.errorreporting.ErrorReporter
import com.projectcitybuild.modules.logger.PlatformLogger
import com.projectcitybuild.modules.textcomponentbuilder.send
import dagger.Reusable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor
import javax.inject.Inject

@Reusable
class BungeecordCommandRegistry @Inject constructor(
    private val plugin: Plugin,
    private val logger: PlatformLogger,
    private val errorReporter: ErrorReporter,
) {
    private class CommandProxy(
        alias: String,
        private val execute: (CommandSender?, List<String>) -> Boolean,
        private val tabComplete: (CommandSender?, List<String>) -> Iterable<String>?
    ) : Command(alias), TabExecutor {

        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            val argsList = args?.toList() ?: emptyList()
            execute(sender, argsList)
        }

        override fun onTabComplete(sender: CommandSender?, args: Array<out String>?): MutableIterable<String> {
            val improvedArgs = args?.filter { it.isNotEmpty() } ?: emptyList()

            val list = tabComplete(sender, improvedArgs) ?: emptyList()
            return list.toMutableList()
        }
    }

    fun register(command: BungeecordCommand) {
        val labels = command.aliases.plus(command.label)
        val prefixedLabels = labels.map { "pcbridge:$it" } // Replicate Spigot's command prefixing in-case of conflicts

        labels.plus(prefixedLabels).forEach { alias ->
            plugin.proxy.pluginManager.registerCommand(
                plugin,
                CommandProxy(
                    alias,
                    execute = { sender, args ->
                        if (sender == null)
                            throw Exception("Attempted to execute command with a null CommandSender")

                        if (!sender.hasPermission(command.permission)) {
                            sender.send().error("You do not have the required permission to use this command")
                            return@CommandProxy true
                        }

                        val input = BungeecordCommandInput(
                            sender = sender,
                            args = args
                        )
                        CoroutineScope(Dispatchers.IO).launch {
                            runCatching {
                                command.execute(input)
                            }.onFailure { throwable ->
                                if (throwable is InvalidCommandArgumentsException) {
                                    sender.sendMessage(
                                        TextComponent(command.usageHelp).also {
                                            it.color = ChatColor.GRAY
                                            it.isItalic = true
                                        }
                                    )
                                } else {
                                    sender.send().error(throwable.message ?: "An internal error occurred performing your command")
                                    throwable.message?.let { logger.fatal(it) }
                                    throwable.printStackTrace()

                                    errorReporter.report(throwable)
                                }
                            }
                        }
                        true
                    },
                    tabComplete = { sender, args -> command.onTabComplete(sender, args) }
                )
            )
        }
    }
}

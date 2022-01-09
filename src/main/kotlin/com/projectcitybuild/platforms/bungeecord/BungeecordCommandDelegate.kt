package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import net.md_5.bungee.api.plugin.TabExecutor

class BungeecordCommandDelegate constructor(
    private val plugin: Plugin,
    private val logger: LoggerProvider
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
             // The command name is given as arg[0] for some reason
            val improvedArgs = args?.drop(1) ?: emptyList()

            val list = tabComplete(sender, improvedArgs) ?: emptyList()
            return list.toMutableList()
        }
    }

    fun register(command: BungeecordCommand) {
        command.aliases.plus(command.label).forEach { alias ->
            plugin.proxy.pluginManager.registerCommand(plugin, CommandProxy(
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
                    runCatching {
                        CoroutineScope(Dispatchers.IO).launch {
                            command.execute(input)
                        }
                    }.onFailure { throwable ->
                        sender.send().error(throwable.message ?: "An internal error occurred performing your command")
                        throwable.message?.let { logger.fatal(it) }
                        throwable.printStackTrace()
                    }
                    true
                },
                tabComplete = { sender, args -> command.onTabComplete(sender, args) }
            ))
        }
    }
}
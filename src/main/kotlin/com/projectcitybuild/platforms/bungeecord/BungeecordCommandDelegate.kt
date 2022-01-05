package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.spigot.send
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin

class BungeecordCommandDelegate constructor(
    private val plugin: Plugin,
    private val logger: LoggerProvider
) {
    private class CommandProxy(
        alias: String,
        private val execute: (CommandSender?, List<String>) -> Boolean
    ) : Command(alias) {
        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            val argsList = args?.toList() ?: emptyList()
            execute(sender, argsList)
        }
    }

    fun register(command: BungeecordCommand) {
        command.aliases.plus(command.label).forEach { alias ->
            plugin.proxy.pluginManager.registerCommand(plugin, CommandProxy(alias) { sender, args ->
                if (sender == null)
                    throw Exception("Attempted to execute command with a null CommandSender")

                val input = BungeecordCommandInput(
                    sender = sender,
                    args = args
                )
                when (command.validate(input)) {
                    CommandResult.INVALID_INPUT -> false
                    CommandResult.EXECUTED -> {
                        runCatching {
                            GlobalScope.launch { command.execute(input) }
                        }.onFailure { throwable ->
                            sender?.send()?.error(throwable.message ?: "An internal error occurred performing your command")
                            throwable.message?.let { logger.fatal(it) }
                            throwable.printStackTrace()
                        }
                        true
                    }
                }
            })

            // FIXME
//            plugin.getCommand(alias)?.permission = command.permission
        }
    }
}
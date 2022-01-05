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
        private val handler: (CommandSender?, Array<out String>?) -> Boolean
    ) : Command(alias) {
        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            handler(sender, args)
        }
    }

    fun register(command: BungeecordCommand) {
        command.aliases.plus(command.label).forEach { alias ->
            plugin.proxy.pluginManager.registerCommand(plugin, CommandProxy(alias) { sender, args ->
                runCatching {
                    if (sender == null) throw Exception("Attempted to execute command with a null CommandSender")

                    val input = BungeecordCommandInput(
                        sender = sender,
                        args = args?.toList() ?: emptyList()
                    )
                    GlobalScope.launch {
                        val result = command.execute(input)
                        when (result) {
                            CommandResult.EXECUTED -> true
                            CommandResult.INVALID_INPUT -> false
                        }
                    }
                }.onFailure { error ->
                    sender?.send()?.error(error.message ?: "An internal error occurred performing your command")
                    error.localizedMessage.let { message -> logger.fatal(message) }
                    error.printStackTrace()
                    true
                }
            })

            // FIXME
//            plugin.getCommand(alias)?.permission = command.permission
        }
    }
}
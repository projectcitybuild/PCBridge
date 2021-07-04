package com.projectcitybuild.platforms.bungeecord

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin

class BungeecordCommandDelegate constructor(
        private val plugin: Plugin,
        private val logger: LoggerProvider
) {
    private class CommandProxy(
            alias: String,
            private val handler: (CommandSender?, Array<out String>?) -> Boolean
    ): Command(alias) {
        override fun execute(sender: CommandSender?, args: Array<out String>?) {
            handler(sender, args)
        }
    }

    fun register(command: BungeecordCommand) {
        command.aliases.plus(command.label).forEach { alias ->
            val handler = CommandProxy(alias) { sender, args ->
                try {
                    val input = BungeecordCommandInput(
                            sender = sender,
                            args = args?.map { arg -> arg } ?: listOf()
                    )
                    when (command.execute(input)) {
                        CommandResult.EXECUTED -> true
                        CommandResult.INVALID_INPUT -> false
                    }
                } catch (error: Exception) {
                    sender?.sendMessage(TextComponent("An internal error occurred performing your command").also {
                        it.color = ChatColor.RED
                    })
                    error.localizedMessage.let { message -> logger.fatal(message) }
                    true
                }
            }
            plugin.proxy.pluginManager.registerCommand(plugin, handler)

            // FIXME
//            plugin.getCommand(alias)?.permission = command.permission
        }
    }
}
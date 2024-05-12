package com.projectcitybuild.pcbridge.features.utilities.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.projectcitybuild.pcbridge.support.messages.CommandHelpBuilder
import com.projectcitybuild.pcbridge.support.spigot.BadCommandUsageException
import com.projectcitybuild.pcbridge.support.spigot.CommandArgsParser
import com.projectcitybuild.pcbridge.support.spigot.SpigotCommand
import com.projectcitybuild.pcbridge.support.tryValueOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class PCBridgeCommand(
    private val plugin: JavaPlugin,
): SpigotCommand<PCBridgeCommand.Args> {
    override val label = "pcbridge"

    override val usage = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, args: Args) {
        when (args.command) {
            Args.Command.Reload -> reload(sender)
        }
    }

    private fun reload(sender: CommandSender) {
        plugin.onDisable()
        plugin.onEnable()

        val message = Component.text()
            .content("Reload complete")
            .color(TextColor.color(0x75e900))
            .build()

        sender.sendMessage(message)
    }

    class TabCompleter: SuspendingTabCompleter {
        private val subcommands = listOf(
            "reload",
        )

        override suspend fun onTabComplete(
            sender: CommandSender,
            command: Command,
            alias: String,
            args: Array<out String>
        ): List<String>? {
            if (args.isEmpty()) {
                return null
            }
            if (args.size == 1) {
                return subcommands
                    .filter { it.startsWith(args[0]) }
                    .toMutableList()
            }
            return null
        }
    }

    data class Args(
        val command: Command,
        val remainingArgs: List<String>,
    ) {
        enum class Command {
            Reload,
        }
        class Parser: CommandArgsParser<Args> {
            override fun parse(args: List<String>): Args {
                if (args.isEmpty()) {
                    throw BadCommandUsageException()
                }
                val command = tryValueOf<Command>(args[0].replaceFirstChar { it.uppercase() })
                    ?: throw BadCommandUsageException()

                return Args(
                    command = command,
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}
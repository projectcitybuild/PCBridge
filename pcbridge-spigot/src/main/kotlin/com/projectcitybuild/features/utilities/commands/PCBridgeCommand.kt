package com.projectcitybuild.features.utilities.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.projectcitybuild.support.messages.CommandHelpBuilder
import com.projectcitybuild.support.spigot.ArgsParser
import com.projectcitybuild.support.spigot.SpigotCommand
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class PCBridgeCommand(
    private val plugin: JavaPlugin,
    private val audiences: BukkitAudiences,
): SpigotCommand<PCBridgeCommand.Args> {
    override val usage: CommandHelpBuilder
        get() = CommandHelpBuilder() // TODO

    override suspend fun run(sender: CommandSender, command: Command, args: Args) {
        when (args.command) {
            "reload" -> reload(sender)
            else -> displayUsage(sender, audiences)
        }
    }

    private fun reload(sender: CommandSender) {
        plugin.onDisable()
        plugin.onEnable()

        val message = Component.text()
            .content("Reload complete")
            .color(TextColor.color(0x75e900))
            .build()

        audiences.sender(sender).sendMessage(message)
    }

    class TabCompleter: SuspendingTabCompleter {
        private val subCommands = listOf(
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
                return subCommands
                    .filter { it.startsWith(args[0]) }
                    .toMutableList()
            }
            return null
        }
    }

    data class Args(
        val command: String,
        val remainingArgs: List<String>,
    ) {
        class Parser: ArgsParser<Args> {
            override fun tryParse(args: List<String>): Args? {
                if (args.isEmpty()) {
                    return null
                }
                return Args(
                    command = args[0],
                    remainingArgs = args.drop(1),
                )
            }
        }
    }
}
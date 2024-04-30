package com.projectcitybuild.features.utilities.commands

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

class PCBridgeCommand(
    private val plugin: JavaPlugin,
    private val audiences: BukkitAudiences,
): SuspendingCommandExecutor {
    override suspend fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.isEmpty()) {
            return false
        }
        when (args[0]) {
            "reload" -> reload(sender)
        }
        return true
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
}
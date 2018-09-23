package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.command.CommandSender

class PrefixCommand : Commandable {
    override val label: String = "prefix"

    override var environment: Environment? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("Environment missing")
        if (args.isEmpty() || args.size > 2) return false

        val targetPlayerName = args.first()
        val targetPlayer = sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        if (environment?.chat == null) {
            throw Exception("Chat hook unavailable")
        }

        if (args.size == 1) {
            val prefix = environment?.chat?.getPlayerPrefix(targetPlayer)
            sender.sendMessage("Prefix for ${targetPlayer.name}: $prefix")
            return true
        }
        if (args.size == 2) {
            val prefix = args[1]
            environment?.chat?.setPlayerPrefix(targetPlayer, prefix)
            sender.sendMessage("${targetPlayer.name} prefix set to $prefix")
        }

        return true
    }
}
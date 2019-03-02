package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import org.bukkit.command.CommandSender

class SuffixCommand : Commandable {
    override val label: String = "suffix"
    override val permission: String = "pcbridge.chat.suffix"

    override var environment: EnvironmentProvider? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("EnvironmentProvider missing")
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
            environment?.chat?.setPlayerSuffix(targetPlayer, null)
            sender.sendMessage("${targetPlayer.name} suffix reset")
            return true
        }
        if (args.size == 2) {
            val suffix = args[1]
            environment?.chat?.setPlayerSuffix(targetPlayer, suffix)
            sender.sendMessage("${targetPlayer.name} suffix set to $suffix")
        }

        return true
    }
}
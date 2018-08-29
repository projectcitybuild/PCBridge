package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.command.CommandSender

class MuteCommand : Commandable {
    override val label: String = "mute"

    override var environment: Environment? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("Environment missing")
        if (args.isEmpty()) return false

        val targetPlayerName = args.first()
        val targetPlayer = sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        val player = environment?.get(targetPlayer.uniqueId)
            ?: throw Exception("Player ${targetPlayerName} missing from cache")

        if (player.isMuted) {
            sender.sendMessage("$targetPlayerName is already muted")
            return true
        }

        player.isMuted = true
        environment?.set(player)

        sender.sendMessage("${targetPlayer.name} has been muted")
        targetPlayer.sendMessage("You have been muted by ${sender.name}")

        return true
    }
}
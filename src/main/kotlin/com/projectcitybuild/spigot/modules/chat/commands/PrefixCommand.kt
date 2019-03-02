package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PrefixCommand : Commandable {
    override val label: String = "prefix"
    override val permission: String = "pcbridge.chat.prefix"

    override var environment: EnvironmentProvider? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("EnvironmentProvider missing")
        if (args.size > 3) return false

        val targetPlayerName = args.first()
        val targetPlayer = sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        if (environment?.chat == null) {
            throw Exception("Chat hook unavailable")
        }

        val action = args[1]
        val newPrefix = args[2]

        when (action) {
            null -> getPrefixFor(player = targetPlayer, sender = sender)
            "set" -> setPrefixFor(player = targetPlayer, sender = sender, newPrefix = newPrefix)
            "clear" -> clearPrefixFor(player = targetPlayer, sender = sender)
        }

        return true
    }

    private fun setPrefixFor(player: Player, sender: CommandSender, newPrefix: String) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        playerConfig.prefix = newPrefix
        sender.sendMessage("Updated prefix for ${player.name}")
    }

    private fun getPrefixFor(player: Player, sender: CommandSender) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        if (playerConfig.prefix == null) {
            sender.sendMessage("${player.name} does not have a prefix set")
        } else {
            sender.sendMessage("Prefix for ${player.name}: ${playerConfig.prefix}")
        }
    }

    private fun clearPrefixFor(player: Player, sender: CommandSender) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        if (playerConfig.prefix == null) {
            sender.sendMessage("${player.displayName} does not have a prefix set")
            return
        }
        playerConfig.prefix = null
        sender.sendMessage("Updated prefix for ${player.displayName}")
    }
}
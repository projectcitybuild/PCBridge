package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SuffixCommand : Commandable {
    override val label: String = "suffix"
    override val permission: String = "pcbridge.chat.suffix"

    override var environment: EnvironmentProvider? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("EnvironmentProvider missing")
        if (args.size < 1 || args.size > 3) return false

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
            getSuffixFor(player = targetPlayer, sender = sender)
            return true
        }

        when (args[1]) {
            "set" -> {
                if (args.size != 3) {
                    return false
                }
                val newSuffix = args[2]
                setSuffixFor(player = targetPlayer, sender = sender, newSuffix = newSuffix)
            }
            "clear" -> {
                clearSuffixFor(player = targetPlayer, sender = sender)
            }
            else -> return false
        }

        return true
    }

    private fun setSuffixFor(player: Player, sender: CommandSender, newSuffix: String) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        playerConfig.suffix = newSuffix
        environment?.set(playerConfig)

        sender.sendMessage("Updated suffix for ${player.name}")
    }

    private fun getSuffixFor(player: Player, sender: CommandSender) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        if (playerConfig.prefix == null) {
            sender.sendMessage("${player.name} does not have a suffix set")
        } else {
            sender.sendMessage("Suffix for ${player.name}: ${playerConfig.suffix}")
        }
    }

    private fun clearSuffixFor(player: Player, sender: CommandSender) {
        val playerConfig = environment?.get(player = player.uniqueId)
        if (playerConfig == null) {
            return
        }
        if (playerConfig.suffix == null) {
            sender.sendMessage("${player.displayName} does not have a suffix set")
            return
        }
        playerConfig.suffix = null
        environment?.set(playerConfig)
        
        sender.sendMessage("Updated suffix for ${player.displayName}")
    }
}
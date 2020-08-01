package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.api.APIProvider
import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import org.bukkit.command.CommandSender

class UnmuteCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override var apiProvider: APIProvider? = null

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        if (environment == null) throw Exception("EnvironmentProvider missing")
        if (args.isEmpty()) return false

        val targetPlayerName = args.first()
        val targetPlayer = sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        val player = environment?.get(targetPlayer.uniqueId)
            ?: throw Exception("Player $targetPlayerName missing from cache")

        if (!player.isMuted) {
            sender.sendMessage("$targetPlayerName is not muted")
            return true
        }

        player.isMuted = false
        environment?.set(player)

        sender.sendMessage("${targetPlayer.name} has been unmuted")
        targetPlayer.sendMessage("You have been unmuted by ${sender.name}")

        return true
    }
}
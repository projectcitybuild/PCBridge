package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.core.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class MuteCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "mute"

    override fun execute(sender: CommandSender?, command: Command?, label: String?, args: Array<String>?): Boolean {
        val targetPlayerName = args?.first() ?: return false

        val targetPlayer = sender?.server?.getOnlinePlayer(targetPlayerName)
        if (targetPlayer == null) {
            sender?.sendMessage("Failed to mute: Player not found")
            return true
        }

        val environment = environment!!
        val player = environment.get(targetPlayer.uniqueId)

        if (player?.isMuted == true) {
            sender.sendMessage("Failed to mute: That player is already muted")
            return true
        }

        player?.isMuted = true
        environment.set(player!!)

        sender.sendMessage("${targetPlayer.name} has been muted")
        targetPlayer.sendMessage("You have been muted by ${sender.name}")

        return true
    }
}
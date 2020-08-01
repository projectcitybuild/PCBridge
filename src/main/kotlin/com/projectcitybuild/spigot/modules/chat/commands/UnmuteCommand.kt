package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.CommandInput

class UnmuteCommand(
        private val environment: EnvironmentProvider
): Commandable {

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"

    override fun execute(input: CommandInput): Boolean {
        if (input.args.isEmpty()) return false

        val targetPlayerName = input.args.first()
        val targetPlayer = input.sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            input.sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        val player = environment?.get(targetPlayer.uniqueId)
            ?: throw Exception("Player $targetPlayerName missing from cache")

        if (!player.isMuted) {
            input.sender.sendMessage("$targetPlayerName is not muted")
            return true
        }

        player.isMuted = false
        environment?.set(player)

        input.sender.sendMessage("${targetPlayer.name} has been unmuted")
        targetPlayer.sendMessage("You have been unmuted by ${input.sender.name}")

        return true
    }
}
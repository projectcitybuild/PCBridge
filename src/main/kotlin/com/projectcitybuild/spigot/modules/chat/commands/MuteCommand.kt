package com.projectcitybuild.spigot.modules.chat.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.spigot.extensions.getOnlinePlayer

class MuteCommand(
        private val environment: EnvironmentProvider
): Commandable {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"

    override fun execute(input: CommandInput): Boolean {
        if (input.args.isEmpty()) return false

        val targetPlayerName = input.args.first()
        val targetPlayer = input.sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            input.sender.sendMessage("Player $targetPlayerName not found")
            return true
        }

        val player = environment.get(targetPlayer.uniqueId)
            ?: throw Exception("Player ${targetPlayerName} missing from cache")

        if (player.isMuted) {
            input.sender.sendMessage("$targetPlayerName is already muted")
            return true
        }

        player.isMuted = true
        environment.set(player)

        input.sender.sendMessage("${targetPlayer.name} has been muted")
        targetPlayer.sendMessage("You have been muted by ${input.sender.name}")

        return true
    }
}
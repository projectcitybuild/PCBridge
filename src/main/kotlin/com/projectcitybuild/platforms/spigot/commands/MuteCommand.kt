package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.utilities.PlayerStore
import com.projectcitybuild.platforms.spigot.environment.send
import com.projectcitybuild.platforms.spigot.extensions.getOnlinePlayer

class MuteCommand(
        private val playerStore: PlayerStore
): Commandable {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()
        val targetPlayer = input.sender.server?.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return CommandResult.INVALID_INPUT
        }

        val player = playerStore.get(targetPlayer.uniqueId)
            ?: throw Exception("Player $targetPlayerName missing from cache")

        if (player.isMuted) {
            input.sender.send().error("$targetPlayerName is already muted")
            return CommandResult.INVALID_INPUT
        }

        player.isMuted = true
        playerStore.put(player.uuid, player)

        input.sender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.sendMessage("You have been muted by ${input.sender.name}")

        return CommandResult.EXECUTED
    }
}
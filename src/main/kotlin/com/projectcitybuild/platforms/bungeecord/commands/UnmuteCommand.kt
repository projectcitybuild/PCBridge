package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.entities.CommandInput

class UnmuteCommand(
//        private val playerStore: PlayerStore
): Commandable {

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"

    override suspend fun execute(input: CommandInput): CommandResult {
//        if (!input.hasArguments) return CommandResult.INVALID_INPUT
//
//        val targetPlayerName = input.args.first()
//        val targetPlayer = input.sender.server?.getOnlinePlayer(name = targetPlayerName)
//        if (targetPlayer == null) {
//            input.sender.send().error("Player $targetPlayerName not found")
//            return CommandResult.INVALID_INPUT
//        }
//
//        val player = playerStore.get(targetPlayer.uniqueId)
//            ?: throw Exception("Player $targetPlayerName missing from cache")
//
//        if (!player.isMuted) {
//            input.sender.send().error("$targetPlayerName is not muted")
//            return CommandResult.INVALID_INPUT
//        }
//
//        player.isMuted = false
//        playerStore.put(player.uuid, player)
//
//        input.sender.send().success("${targetPlayer.name} has been unmuted")
//        targetPlayer.sendMessage("You have been unmuted by ${input.sender.name}")

        return CommandResult.EXECUTED
    }
}
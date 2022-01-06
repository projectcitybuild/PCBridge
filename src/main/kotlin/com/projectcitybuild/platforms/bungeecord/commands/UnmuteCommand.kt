package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ProxyServer

class UnmuteCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"

    override fun validate(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty())
            return CommandResult.INVALID_INPUT

        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
        val targetPlayerName = input.args.first()
        val targetPlayer = proxyServer.players
            .first { it.name.lowercase() == targetPlayerName.lowercase() }

        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        val player = playerConfigRepository.get(targetPlayer.uniqueId).also {
            it.isMuted = false
        }
        playerConfigRepository.save(player)

        input.sender.send().success("${targetPlayer.name} has been unmuted")
        targetPlayer.send().info("You have been unmuted by ${input.sender.name}")
    }
}
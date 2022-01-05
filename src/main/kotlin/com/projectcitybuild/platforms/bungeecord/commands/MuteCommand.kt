package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ProxyServer

class MuteCommand(
    private val proxyServer: ProxyServer,
    private val playerRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"

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

        val player = playerRepository.get(targetPlayer.uniqueId).also {
            it.isMuted = true
        }
        playerRepository.save(player)

        input.sender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.send().info("You have been muted by ${input.sender.name}")
    }
}
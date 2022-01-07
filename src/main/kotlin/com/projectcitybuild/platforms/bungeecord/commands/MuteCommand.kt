package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.ProxyServer

class MuteCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"
    override val usageHelp = "/mute <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }

        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName not found")
            return
        }

        val player = playerConfigRepository.get(targetPlayer.uniqueId).also {
            it.isMuted = true
        }
        playerConfigRepository.save(player)

        input.sender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.send().info("You have been muted by ${input.sender.name}")
    }
}
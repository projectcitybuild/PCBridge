package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class UnmuteCommand(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"
    override val usageHelp = "/unmute <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = proxyServer.players
            .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }

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

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
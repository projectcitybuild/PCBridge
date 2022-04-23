package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.repositories.PlayerConfigRepository
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class UnmuteCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val nameGuesser: NameGuesser
) : BungeecordCommand {

    override val label: String = "unmute"
    override val permission: String = "pcbridge.chat.unmute"
    override val usageHelp = "/unmute <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName is not online")
            return
        }

        val targetPlayerConfig = playerConfigRepository
            .get(targetPlayer.uniqueId)!!
            .also { it.isMuted = false }

        playerConfigRepository.save(targetPlayerConfig)

        input.sender.send().success("${targetPlayer.name} has been unmuted")
        targetPlayer.send().info("You have been unmuted by ${input.sender.name}")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}

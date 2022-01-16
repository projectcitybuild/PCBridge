package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.features.chat.repositories.ChatIgnoreRepository
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import javax.inject.Inject

class IgnoreCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val nameGuesser: NameGuesser
): BungeecordCommand {

    override val label = "ignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/ignore <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }
        if (targetPlayer == null) {
            input.player.send().error("Could not find $targetPlayerName online")
            return
        }
        if (input.player == targetPlayer) {
            input.player.send().error("You cannot ignore yourself")
            return
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)
        val targetPlayerConfig = playerConfigRepository.get(targetPlayer.uniqueId)

        if (chatIgnoreRepository.isIgnored(playerConfig!!.id, targetPlayerConfig!!.id)) {
            input.player.send().error("$targetPlayerName is already on your ignore list")
            return
        }

        chatIgnoreRepository.add(playerConfig.id, targetPlayerConfig.id)

        input.sender.send().success("You are now ignoring chat and DMs from $targetPlayerName")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
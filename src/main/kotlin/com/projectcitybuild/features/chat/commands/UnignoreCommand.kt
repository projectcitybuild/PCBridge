package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.entities.PlayerConfig
import com.projectcitybuild.features.chat.repositories.ChatIgnoreRepository
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer
import java.util.*
import javax.inject.Inject

class UnignoreCommand @Inject constructor(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val playerNameGuesser: NameGuesser
): BungeecordCommand {

    override val label = "unignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/unignore <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = playerNameGuesser.guessClosest(targetPlayerName, proxyServer.players) { it.name }

        if (input.player == targetPlayer) {
            input.player.send().error("You cannot ignore yourself")
            return
        }

        val targetPlayerUUID =
            if (targetPlayer != null) {
                targetPlayer.uniqueId
            } else {
                playerUUIDRepository.request(targetPlayerName)
                    ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
            }

        val targetPlayerConfig = playerConfigRepository.get(targetPlayerUUID)
        if (targetPlayerConfig == null) {
            input.player.send().error("$targetPlayerName has never been on the server before")
            return
        }

        val playerConfig = playerConfigRepository.get(input.player.uniqueId)
        if (!chatIgnoreRepository.isIgnored(playerConfig!!.id, targetPlayerConfig.id)) {
            input.player.send().error("$targetPlayerName is not on your ignore list")
            return
        }

        chatIgnoreRepository.delete(playerConfig.id, targetPlayerConfig.id)

        input.sender.send().success("You will now see chat and DMs from $targetPlayerName again")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
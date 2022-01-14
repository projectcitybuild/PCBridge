package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class UnignoreCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "unignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/unignore <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()

        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        runCatching {
            val targetPlayerUUID = playerUUIDRepository.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            if (input.player.uniqueId == targetPlayerUUID) {
                input.player.send().error("You cannot ignore yourself")
                return
            }

            val playerConfig = playerConfigRepository.get(input.player.uniqueId)
            if (!playerConfig.chatIgnoreList.contains(targetPlayerUUID)) {
                input.player.send().error("$targetPlayerName is not on your ignore list")
                return@runCatching
            }

            playerConfig.chatIgnoreList.removeIf { it == targetPlayerUUID }
            playerConfigRepository.save(playerConfig)

            input.sender.send().success("You will now see chat from $targetPlayerName again")

        }.onFailure { throwable ->
            input.sender.send().error(throwable.message ?: "An unknown error occurred")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.lowercase().startsWith(args.first()) }
            else -> null
        }
    }
}
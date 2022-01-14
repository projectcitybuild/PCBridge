package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.old_modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.playeruuid.PlayerUUIDRepository
import com.projectcitybuild.entities.serializables.SerializableUUID
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.modules.textcomponentbuilder.send
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.ProxyServer

class IgnoreCommand(
    private val proxyServer: ProxyServer,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "ignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/ignore <name>"

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
            if (playerConfig.unwrappedChatIgnoreList.contains(targetPlayerUUID)) {
                input.player.send().error("$targetPlayerName is already on your ignore list")
                return
            }

            playerConfig.chatIgnoreList.add(SerializableUUID(targetPlayerUUID))
            playerConfigRepository.save(playerConfig)

            input.sender.send().success("You are now ignoring chat from ${targetPlayerName}")

        }.onFailure { throwable ->
            input.sender.send().error(throwable.message ?: "An unknown error occurred")
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> proxyServer.players.map { it.name }
            args.size == 1 -> proxyServer.players.map { it.name }.filter { it.startsWith(args.first()) }
            else -> null
        }
    }
}
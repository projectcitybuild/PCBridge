package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookupService
import com.projectcitybuild.modules.storage.SerializableUUID
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send

class IgnoreCommand(
    private val playerUUIDLookupService: PlayerUUIDLookupService,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "ignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/ignore <name>"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.args.size != 1) {
            input.sender.send().invalidCommandInput(this)
            return
        }

        val targetPlayerName = input.args.first()

        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        runCatching {
            val targetPlayerUUID = playerUUIDLookupService.request(targetPlayerName)
                ?: throw Exception("Could not find UUID for $targetPlayerName. This player likely doesn't exist")

            val playerConfig = playerConfigRepository.get(input.player.uniqueId)
            if (playerConfig.unwrappedChatIgnoreList.contains(targetPlayerUUID)) {
                input.player.send().error("$targetPlayerName is already on your ignore list")
                return@runCatching
            }

            playerConfig.chatIgnoreList.add(SerializableUUID(targetPlayerUUID))
            playerConfigRepository.save(playerConfig)

            input.sender.send().success("You are now ignoring chat from ${targetPlayerName}")

        }.onFailure { throwable ->
            input.sender.send().error(throwable.message ?: "An unknown error occurred")
        }
    }
}
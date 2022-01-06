package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.modules.playerconfig.PlayerConfigRepository
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.modules.storage.SerializableUUID
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send

class IgnoreCommand(
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val playerConfigRepository: PlayerConfigRepository
): BungeecordCommand {

    override val label = "ignore"
    override val permission = "pcbridge.chat.ignore"

    override fun validate(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty() || input.args.size > 1)
            return CommandResult.INVALID_INPUT

        return CommandResult.EXECUTED
    }

    override suspend fun execute(input: BungeecordCommandInput) {
        val targetPlayerName = input.args.first()

        if (input.player == null) {
            input.sender.send().error("Console cannot use this command")
            return
        }

        runCatching {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
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
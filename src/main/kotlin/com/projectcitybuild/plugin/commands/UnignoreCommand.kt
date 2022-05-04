package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.CannotInvokeFromConsoleException
import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import com.projectcitybuild.repositories.PlayerUUIDRepository
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class UnignoreCommand @Inject constructor(
    private val server: Server,
    private val playerUUIDRepository: PlayerUUIDRepository,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val playerNameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "unignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/unignore <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            throw CannotInvokeFromConsoleException()
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = playerNameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }

        if (input.player == targetPlayer) {
            input.player.send().error("You cannot ignore yourself")
            return
        }

        val targetPlayerUUID =
            if (targetPlayer != null) {
                targetPlayer.uniqueId
            } else {
                playerUUIDRepository.get(targetPlayerName)
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
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}

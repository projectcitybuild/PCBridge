package com.projectcitybuild.features.chat.commands

import com.projectcitybuild.core.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.ChatIgnoreRepository
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class IgnoreCommand @Inject constructor(
    private val server: Server,
    private val playerConfigRepository: PlayerConfigRepository,
    private val chatIgnoreRepository: ChatIgnoreRepository,
    private val nameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "ignore"
    override val permission = "pcbridge.chat.ignore"
    override val usageHelp = "/ignore <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.isConsole) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }
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
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}

package com.projectcitybuild.plugin.commands

import com.projectcitybuild.core.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import com.projectcitybuild.repositories.PlayerConfigRepository
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class MuteCommand @Inject constructor(
    private val server: Server,
    private val playerConfigRepository: PlayerConfigRepository,
    private val nameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "mute"
    override val permission = "pcbridge.chat.mute"
    override val usageHelp = "/mute <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }
        if (targetPlayer == null) {
            input.sender.send().error("Player $targetPlayerName is not online")
            return
        }

        val targetPlayerConfig = playerConfigRepository
            .get(targetPlayer.uniqueId)!!
            .also { it.isMuted = true }

        playerConfigRepository.save(targetPlayerConfig)

        input.sender.send().success("${targetPlayer.name} has been muted")
        targetPlayer.send().info("You have been muted by ${input.sender.name}")
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}

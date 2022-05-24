package com.projectcitybuild.plugin.commands

import com.projectcitybuild.plugin.exceptions.InvalidCommandArgumentsException
import com.projectcitybuild.core.utilities.Failure
import com.projectcitybuild.core.utilities.Success
import com.projectcitybuild.features.ranksync.usecases.UpdatePlayerGroupsUseCase
import com.projectcitybuild.modules.nameguesser.NameGuesser
import com.projectcitybuild.modules.textcomponentbuilder.send
import com.projectcitybuild.plugin.environment.SpigotCommand
import com.projectcitybuild.plugin.environment.SpigotCommandInput
import org.bukkit.Server
import org.bukkit.command.CommandSender
import javax.inject.Inject

class SyncOtherCommand @Inject constructor(
    private val server: Server,
    private val updatePlayerGroupsUseCase: UpdatePlayerGroupsUseCase,
    private val nameGuesser: NameGuesser
) : SpigotCommand {

    override val label = "syncother"
    override val permission = "pcbridge.sync.other"
    override val usageHelp = "/syncother <name>"

    override suspend fun execute(input: SpigotCommandInput) {
        if (input.args.size != 1) {
            throw InvalidCommandArgumentsException()
        }

        val targetPlayerName = input.args.first()
        val targetPlayer = nameGuesser.guessClosest(targetPlayerName, server.onlinePlayers) { it.name }

        if (targetPlayer == null) {
            input.sender.send().error("$targetPlayerName is not online")
            return
        }

        val result = updatePlayerGroupsUseCase.sync(targetPlayer.uniqueId)

        when (result) {
            is Failure -> input.sender.send().error(
                when (result.reason) {
                    UpdatePlayerGroupsUseCase.FailureReason.ACCOUNT_NOT_LINKED
                    -> "Sync failed: Player does not have a linked PCB account"

                    UpdatePlayerGroupsUseCase.FailureReason.PERMISSION_USER_NOT_FOUND
                    -> "Permission user not found. Check that the user exists in the Permission plugin"
                }
            )
            is Success -> {
                input.sender.send().success("$targetPlayerName has been synchronized")
                targetPlayer.send().success("Your account groups have been synchronized")
            }
        }
    }

    override fun onTabComplete(sender: CommandSender?, args: List<String>): Iterable<String>? {
        return when {
            args.isEmpty() -> server.onlinePlayers.map { it.name }
            args.size == 1 -> server.onlinePlayers.map { it.name }.filter { it.lowercase().startsWith(args.first().lowercase()) }
            else -> null
        }
    }
}

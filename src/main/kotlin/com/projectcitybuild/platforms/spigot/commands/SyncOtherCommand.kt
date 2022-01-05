package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.Success
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.send

class SyncOtherCommand(
    private val syncPlayerGroupAction: SyncPlayerGroupAction
): Commandable {

    override val label: String = "syncother"
    override val permission: String = "pcbridge.sync.other"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments || input.args.size > 1) {
            return CommandResult.INVALID_INPUT
        }

        val playerName = input.args.first()
        val player = input.sender.server.onlinePlayers
            .first { it.name.lowercase() == playerName.lowercase() }

        if (player == null) {
            input.sender.send().error("$playerName is not online")
            return CommandResult.EXECUTED
        }

        runCatching {
            syncPlayerGroupAction.execute(player.uniqueId)
        }.onFailure { throwable ->
            player.send().error(
                when (throwable) {
                    is SyncPlayerGroupAction.AccountNotLinkedException -> "Sync failed: Player does not have a linked PCB account"
                    is SyncPlayerGroupAction.PermissionUserNotFoundException -> "Permission user not found. Check that the user exists in the Permission plugin"
                    else -> throwable.message ?: "An unknown error occurred"
                }
            )
            return CommandResult.EXECUTED
        }

        input.sender.send().success("$playerName has been synchronized")
        player.send().success("Your account groups have been synchronized")

        return CommandResult.EXECUTED
    }
}

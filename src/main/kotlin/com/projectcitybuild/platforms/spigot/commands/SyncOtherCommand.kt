package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor

class SyncOtherCommand(
        private val syncPlayerGroupAction: SyncPlayerGroupAction
): Commandable {

    override val label: String = "syncother"
    override val permission: String = "pcbridge.sync.other"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) {
            return CommandResult.INVALID_INPUT
        }
        if (input.args.size > 1) {
            return CommandResult.INVALID_INPUT
        }

        val playerName = input.args.first()
        val player = input.sender.server.getOnlinePlayer(playerName)
        if (player == null) {
            input.sender.send().error("$playerName is not online")
            return CommandResult.EXECUTED
        }

        val result = syncPlayerGroupAction.execute(player.uniqueId)

        when (result) {
            is Success -> {
                input.sender.send().success("${ChatColor.GREEN}$playerName has been synchronized")
                player.sendMessage("${ChatColor.GREEN}Your account groups have been synchronized")
            }
            is Failure -> {
                input.sender.send().error(
                    when (result.reason) {
                        is SyncPlayerGroupAction.FailReason.AccountNotLinked -> "Sync failed: Player does not have a linked PCB account"
                        is SyncPlayerGroupAction.FailReason.NetworkError -> "Failed to contact auth server. Please try again later"
                        is SyncPlayerGroupAction.FailReason.HTTPError -> "Sync failed. Please contact an admin"
                        is SyncPlayerGroupAction.FailReason.PermissionUserNotFound -> "Permission user not found. Check that the user exists in the Permission plugin"
                    }
                )
            }
        }
        return CommandResult.EXECUTED
    }
}

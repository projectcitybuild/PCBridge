package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.extensions.getOnlinePlayer
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
            input.sender.sendMessage("$playerName is not online")
            return CommandResult.EXECUTED
        }

        val result = syncPlayerGroupAction.execute(player.uniqueId)

        when (result) {
            is Success -> {
                input.sender.sendMessage("${ChatColor.GREEN}$playerName has been synchronized")
                player.sendMessage("${ChatColor.GREEN}Your account groups have been synchronized")
            }
            is Failure -> {
                when (result.reason) {
                    is SyncPlayerGroupAction.FailReason.AccountNotLinked ->
                        input.sender.sendMessage("${ChatColor.RED}Sync failed: Player does not have a linked PCB account")

                    else -> input.sender.sendMessage("${ChatColor.RED}Failed to contact auth server")
                }
            }
        }
        return CommandResult.EXECUTED
    }
}

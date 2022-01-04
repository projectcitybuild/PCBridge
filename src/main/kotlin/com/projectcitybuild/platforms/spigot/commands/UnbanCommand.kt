package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.platforms.spigot.send
import org.bukkit.entity.Player

class UnbanCommand(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Commandable {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsole) null else input.sender as Player

        val uuid = input.sender.server.getOfflinePlayer(
            name = targetPlayerName,
            apiRequestFactory = apiRequestFactory,
            apiClient = apiClient
        )
        if (uuid == null) {
            input.sender.send().error("Failed to retrieve UUID of given player")
            return CommandResult.EXECUTED
        }

        val action = CreateUnbanAction(apiRequestFactory, apiClient)
        val result = action.execute(
                playerId = uuid,
                staffId = staffPlayer?.uniqueId
        )
        when (result) {
            is Failure -> {
                input.sender.send().error(
                    when (result.reason) {
                        is CreateUnbanAction.FailReason.PlayerNotBanned -> "$targetPlayerName is not currently banned"
                        is CreateUnbanAction.FailReason.HTTPError -> "Bad request sent to the ban server. Please contact an admin"
                        is CreateUnbanAction.FailReason.NetworkError -> "Failed to contact auth server. Please contact an admin"
                    }
                )
            }
            is Success -> {
                input.sender.server.broadcast("${input.args.first()} has been unbanned", "*")
            }
        }

        return CommandResult.EXECUTED
    }
}
package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
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
            input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
            return CommandResult.EXECUTED
        }

        val action = CreateUnbanAction(apiRequestFactory)
        val result = action.execute(
                playerId = uuid,
                staffId = staffPlayer?.uniqueId
        )
        if (result is CreateUnbanAction.Result.FAILED) {
            val message = when (result.reason) {
                CreateUnbanAction.Failure.PLAYER_NOT_BANNED -> "${input.args.first()} is not currently banned"
                CreateUnbanAction.Failure.BAD_REQUEST -> "Bad request sent to the ban server. Please contact an administrator to have this fixed"
                CreateUnbanAction.Failure.DESERIALIZE_FAILED -> "Error: Bad response received from the ban server. Please contact an admin"
            }
            input.sender.sendMessage(message)
        }
        if (result is CreateUnbanAction.Result.SUCCESS) {
            input.sender.server.broadcast("${input.args.first()} has been unbanned", "*")
        }

        return CommandResult.EXECUTED
    }
}
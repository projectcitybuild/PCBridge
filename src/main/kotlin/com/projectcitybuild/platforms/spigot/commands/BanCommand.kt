package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import org.bukkit.ChatColor
import org.bukkit.entity.Player

class BanCommand(
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient
): Commandable {

    override val label: String = "ban"
    override val permission: String = "pcbridge.ban.ban"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val staffPlayer = if (input.isConsole) null else input.sender as Player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        val uuid = input.sender.server.getOfflinePlayer(
            name = targetPlayerName,
            apiRequestFactory = apiRequestFactory,
            apiClient = apiClient
        )

        if (uuid == null) {
            input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
            return CommandResult.EXECUTED
        }

        val action = CreateBanAction(apiRequestFactory)
        val result = action.execute(
            playerId = uuid,
            playerName = targetPlayerName,
            staffId = staffPlayer?.uniqueId,
            reason = reason
        )

        when (result) {
            is CreateBanAction.Result.FAILED -> {
                val message = when (result.reason) {
                    CreateBanAction.Failure.PLAYER_ALREADY_BANNED -> "${input.args.first()} is already banned"
                    CreateBanAction.Failure.BAD_REQUEST -> "Bad request sent to the ban server. Please contact an administrator to have this fixed"
                    CreateBanAction.Failure.DESERIALIZE_FAILED -> "Error: Bad response received from the ban server. Please contact an admin"
                }
                input.sender.sendMessage(message)
            }

            is CreateBanAction.Result.SUCCESS -> {
                input.sender.server.broadcast(
                    "${ChatColor.GRAY}${input.args.first()} has been banned by ${input.sender.name}: ${reason ?: "No reason given"}",
                    "*"
                )

                val player = input.sender.server.onlinePlayers.first { player ->
                    player.name.lowercase() == targetPlayerName.lowercase()
                }
                player?.kickPlayer("You have been banned")
            }
        }

        return CommandResult.EXECUTED
    }
}
package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.platforms.spigot.send
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

        val action = CreateBanAction(apiRequestFactory, apiClient)
        val result = action.execute(
            playerId = uuid,
            playerName = targetPlayerName,
            staffId = staffPlayer?.uniqueId,
            reason = reason
        )

        when (result) {
            is Failure -> {
                input.sender.send().error(
                    when (result.reason) {
                        is CreateBanAction.FailReason.HTTPError -> "Bad response received from the ban server. Please contact an admin"
                        is CreateBanAction.FailReason.NetworkError -> "Failed to contact auth server. Please contact an admin"
                        is CreateBanAction.FailReason.PlayerAlreadyBanned -> "$targetPlayerName is already banned"
                    }
                )
            }
            is Success -> {
                input.sender.server.broadcast(
                    "${ChatColor.GRAY}${input.args.first()} has been banned by ${input.sender.name}: ${reason?.isNotEmpty() ?: "No reason given"}",
                    "*"
                )
                val player = input.sender.server.onlinePlayers.firstOrNull { player ->
                    player.name.lowercase() == targetPlayerName.lowercase()
                }
                player?.kickPlayer("You have been banned")
            }
        }

        return CommandResult.EXECUTED
    }
}
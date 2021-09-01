package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import org.bukkit.ChatColor
import org.bukkit.Server
import java.util.*

class CheckBanCommand(
        private val scheduler: SchedulerProvider,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val checkBanStatusAction: CheckBanStatusAction
) : Commandable {

    override val label: String = "checkban"
    override val permission: String = "pcbridge.ban.checkban"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()

        input.sender.sendMessage("${ChatColor.GRAY}Searching for active bans for $targetPlayerName...")

        val uuid = input.sender.server.getOfflinePlayer(
            name = targetPlayerName,
            apiRequestFactory = apiRequestFactory,
            apiClient = apiClient
        )
        if (uuid == null) {
            input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
            return CommandResult.EXECUTED
        }

        val currentBan = checkBanStatusAction.execute(playerId = uuid)
        when (currentBan) {
            is Success -> {
                val ban = currentBan.value
                if (ban == null) {
                    input.sender.sendMessage("$targetPlayerName is not currently banned")
                } else {
                    input.sender.sendMessage("""
                            #$targetPlayerName is currently banned.
                            #---
                            #Reason: ${ban.reason}
                            #Date: ${ban.createdAt}
                            #Expires: ${ban.expiresAt ?: "Never"}
                        """.trimMargin("#"))
                }
            }
            is Failure -> {
                when (currentBan.reason) {
                    is CheckBanStatusAction.FailReason.HTTPError -> {
                        input.sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
                    }
                }
            }
        }
        return CommandResult.EXECUTED
    }
}
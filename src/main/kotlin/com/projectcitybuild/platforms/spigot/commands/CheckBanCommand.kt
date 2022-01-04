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
import com.projectcitybuild.platforms.spigot.environment.send
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import org.bukkit.ChatColor
import java.text.SimpleDateFormat
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

        val uuid = input.sender.server.getOfflinePlayer(
            name = targetPlayerName,
            apiRequestFactory = apiRequestFactory,
            apiClient = apiClient
        )
        if (uuid == null) {
            input.sender.send().error("Failed to retrieve UUID of given player")
            return CommandResult.EXECUTED
        }

        val currentBan = checkBanStatusAction.execute(playerId = uuid)
        when (currentBan) {
            is Success -> {
                val ban = currentBan.value
                if (ban == null) {
                    input.sender.send().info("$targetPlayerName is not currently banned")
                } else {
                    val banDate = ban.createdAt?.let {
                        val date = Date(it * 1000)
                        val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        format.format(date)
                    }
                    val expiryDate = ban.expiresAt?.let {
                        val date = Date(it * 1000)
                        val format = SimpleDateFormat("yyyy/MM/dd HH:mm")
                        format.format(date)
                    } ?: "Never"

                    input.sender.send().info("""
                            #${ChatColor.RED}$targetPlayerName is currently banned.
                            #${ChatColor.GRAY}---
                            #${ChatColor.GRAY}Reason: ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date: ${ChatColor.WHITE}$banDate
                            #${ChatColor.GRAY}Expires: ${ChatColor.WHITE}$expiryDate
                        """.trimMargin("#"))
                }
            }
            is Failure -> {
                input.sender.send().error(
                    when (currentBan.reason) {
                        is CheckBanStatusAction.FailReason.HTTPError -> "Bad response received from the ban server. Please contact an admin"
                        is CheckBanStatusAction.FailReason.NetworkError -> "Failed to connect to auth server. Please try again later"
                    }
                )
            }
        }
        return CommandResult.EXECUTED
    }
}
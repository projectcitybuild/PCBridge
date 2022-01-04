package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.entities.Failure
import com.projectcitybuild.entities.Success
import com.projectcitybuild.modules.players.PlayerUUIDLookup
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.async
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import java.text.SimpleDateFormat
import java.util.*

class CheckBanCommand(
    private val playerUUIDLookup: PlayerUUIDLookup,
    private val checkBanStatusAction: CheckBanStatusAction
): BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments)
            return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()

        async {
            val targetPlayerUUID = playerUUIDLookup.request(targetPlayerName)
            if (targetPlayerUUID == null) {
                input.sender.send().error("Could not find UUID for $targetPlayerName. This player likely doesn't exist")
                return@async
            }

            val result = checkBanStatusAction.execute(playerId = targetPlayerUUID)
            when (result) {
                is Failure -> {
                    input.sender.send().error(
                        when (result.reason) {
                            is CheckBanStatusAction.FailReason.HTTPError -> "Bad response received from the ban server. Please contact an admin"
                            is CheckBanStatusAction.FailReason.NetworkError -> "Failed to connect to auth server. Please try again later"
                        }
                    )
                }
                is Success -> {
                    val ban = result.value
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
                            #${ChatColor.GRAY}Reason » ${ChatColor.WHITE}${ban.reason}
                            #${ChatColor.GRAY}Date » ${ChatColor.WHITE}$banDate
                            #${ChatColor.GRAY}Expires » ${ChatColor.WHITE}$expiryDate
                        """.trimMargin("#"), isMultiLine = true)
                    }
                }
            }
        }
        return CommandResult.EXECUTED
    }
}
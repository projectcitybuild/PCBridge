package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.chat.MessageSender
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.async
import com.projectcitybuild.platforms.spigot.send
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.text.SimpleDateFormat
import java.util.*

class CheckBanCommand(
    private val proxyServer: ProxyServer,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val checkBanStatusAction: CheckBanStatusAction
): BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()

        async {
            var targetPlayerUUID = proxyServer.players
                .firstOrNull { it.name.lowercase() == targetPlayerName.lowercase() }
                ?.uniqueId

            if (targetPlayerUUID == null) {
                val result = GetMojangPlayerAction(apiRequestFactory, apiClient).execute(playerName = targetPlayerName)
                targetPlayerUUID = when (result) {
                    is Success -> UUID.fromString(result.value.uuid.toDashFormattedUUID())
                    else -> null
                }
            }

            if (targetPlayerUUID == null) {
                input.sender.sendMessage(TextComponent("Could not find UUID for $targetPlayerName"))
                return@async
            }

            val currentBan = checkBanStatusAction.execute(playerId = targetPlayerUUID)
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
        }
        return CommandResult.EXECUTED
    }
}
package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.bans.CreateUnbanAction
import com.projectcitybuild.modules.players.GetPlayerUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class UnbanCommand(
    private val proxyServer: ProxyServer,
    private val scheduler: SchedulerProvider,
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val logger: LoggerProvider
): BungeecordCommand {

    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty()) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()
        val staffPlayer = if (input.isConsoleSender) null else input.player

        GetPlayerUUIDAction(apiRequestFactory, apiClient).execute(
            proxyServer = proxyServer,
            playerName = targetPlayerName
        ) { result ->
            when (result) {
                is Failure -> {
                    scheduler.sync {
                        input.sender.sendMessage(TextComponent("Error: Failed to retrieve UUID of given player").also {
                            it.color = ChatColor.RED
                        })
                    }
                }
                is Success -> {
                    CreateUnbanAction(apiRequestFactory, apiClient).execute(
                        playerId = result.value,
                        staffId = staffPlayer?.uniqueId
                    ) { result ->
                        scheduler.sync {
                            when (result) {
                                is Success -> {
                                    proxyServer.players.forEach {
                                        it.sendMessage(TextComponent("$targetPlayerName has been unbanned").also {
                                            it.color = ChatColor.AQUA
                                            it.isItalic = true
                                        })
                                    }
                                    logger.info("$targetPlayerName was unbanned by ${input.sender.name}")
                                }
                                is Failure -> {
                                    val message = when (result.reason) {
                                        is CreateUnbanAction.FailReason.PLAYER_NOT_BANNED -> "${input.args.first()} is not currently banned"
                                        is CreateUnbanAction.FailReason.API_ERROR -> "Error: ${result.reason.message}"
                                        is CreateUnbanAction.FailReason.UNHANDLED -> "Error: An internal error has occurred. Please contact an admin to have this fixed"
                                    }
                                    input.sender.sendMessage(TextComponent(message).also {
                                        it.color = ChatColor.RED
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
        return CommandResult.EXECUTED
    }
}
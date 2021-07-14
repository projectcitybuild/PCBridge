package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.players.GetPlayerUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.playerByNameIgnoringCase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent

class BanCommand(
        private val proxyServer: ProxyServer,
        private val scheduler: SchedulerProvider,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider
): BungeecordCommand {

    override val label = "ban"
    override val permission = "pcbridge.ban.ban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty()) return CommandResult.INVALID_INPUT

        val staffPlayer = if(input.isConsoleSender) null else input.player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)

        val targetPlayerName = input.args.first().let {
            // If player is online, match their name casing
            proxyServer.playerByNameIgnoringCase(it)?.name ?: it
        }

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
                    return@execute
                }
                is Success -> {
                    CreateBanAction(apiRequestFactory, apiClient = apiClient).execute(
                            playerId = result.value,
                            playerName = targetPlayerName,
                            staffId = staffPlayer?.uniqueId,
                            reason = reason
                    ) { result ->
                        scheduler.sync {
                            when (result) {
                                is Failure -> {
                                    val message = when (result.reason) {
                                        is CreateBanAction.FailReason.PLAYER_ALREADY_BANNED -> "${input.args.first()} is already banned"
                                        is CreateBanAction.FailReason.API_ERROR -> "Error: ${result.reason.message}"
                                        is CreateBanAction.FailReason.UNHANDLED -> "Error: An internal error has occurred. Please contact an admin to have this fixed"
                                    }
                                    input.sender.sendMessage(TextComponent(message).also {
                                        it.color = ChatColor.RED
                                    })
                                }
                                is Success -> {
                                    proxyServer.players.forEach {
                                        it.sendMessage(TextComponent("$targetPlayerName has been banned").also {
                                            it.color = ChatColor.AQUA
                                            it.isItalic = true
                                        })
                                    }
                                    logger.info("$targetPlayerName was banned by ${input.sender.name}: ${reason ?: "No reason given"}")

                                    val player = proxyServer.players.first { player ->
                                        player.name.lowercase() == targetPlayerName.lowercase()
                                    }
                                    player?.disconnect(TextComponent().also {
                                        it.addExtra(TextComponent("You have been banned\n\n").also {
                                            it.color = ChatColor.RED
                                            it.isBold = true
                                        })
                                        it.addExtra(TextComponent("Appeal @ https://projectcitybuild.com").also {
                                            it.color = ChatColor.AQUA
                                        })
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
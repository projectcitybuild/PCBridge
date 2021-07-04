package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.modules.players.GetPlayerUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.extensions.playerByNameIgnoringCase
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Server
import java.text.SimpleDateFormat
import java.util.*

class BanCommand(
        private val proxyServer: ProxyServer,
        private val scheduler: SchedulerProvider,
        private val networkClients: NetworkClients,
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

        getOfflinePlayerUUID(proxyServer = proxyServer, playerName = targetPlayerName) { result ->
            when (result) {
                is GetPlayerUUIDAction.Result.FAILED -> {
                    scheduler.sync {
                        input.sender.sendMessage(TextComponent("Error: Failed to retrieve UUID of given player").also {
                            it.color = ChatColor.RED
                        })
                    }
                    return@getOfflinePlayerUUID
                }
                is GetPlayerUUIDAction.Result.SUCCESS -> {
                    createBan(
                            playerId = result.uuid,
                            playerName = targetPlayerName,
                            staffId = staffPlayer?.uniqueId,
                            reason = reason
                    ) { result ->
                        scheduler.sync {
                            when (result) {
                                is CreateBanAction.Result.FAILED -> {
                                    val message = when (result.reason) {
                                        CreateBanAction.Failure.PLAYER_ALREADY_BANNED -> "${input.args.first()} is already banned"
                                        CreateBanAction.Failure.BAD_REQUEST -> "Error: Bad request sent to the ban server. Please contact an administrator to have this fixed"
                                        CreateBanAction.Failure.DESERIALIZE_FAILED -> "Error: Unexpected response format. Please contact an admin to have this fixed"
                                        CreateBanAction.Failure.UNEXPECTED_EMPTY_BODY -> "Error: Malformed response. Please contact an admin to have this fixed"
                                        CreateBanAction.Failure.UNHANDLED -> "Error: Unexpected error code. Please contact an administrator to have this fixed"
                                    }
                                    input.sender.sendMessage(message)
                                }

                                is CreateBanAction.Result.SUCCESS -> {
                                    proxyServer.players.forEach {
                                        it.sendMessage(TextComponent("$targetPlayerName has been banned").also {
                                            it.color = ChatColor.RED
                                            it.isItalic = true
                                        })
                                    }
                                    logger.info("$targetPlayerName was banned by ${input.sender.name}: ${reason ?: "No reason given"}")

                                    val player = proxyServer.players.first { player ->
                                        player.name.toLowerCase() == targetPlayerName.toLowerCase()
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

    private fun getOfflinePlayerUUID(proxyServer: ProxyServer, playerName: String, completion: (GetPlayerUUIDAction.Result) -> Unit) {
        scheduler.async<GetPlayerUUIDAction.Result> { resolve ->
            val action = GetPlayerUUIDAction(GetMojangPlayerAction(networkClients))
            val result = action.execute(playerName, proxyServer)
            resolve(result)
        }.startAndSubscribe(completion)
    }

    private fun createBan(playerId: UUID, playerName: String, staffId: UUID?, reason: String?, completion: (CreateBanAction.Result) -> Unit) {
        scheduler.async<CreateBanAction.Result> { resolve ->
            val action = CreateBanAction(networkClients)
            val result = action.execute(playerId, playerName, staffId, reason)
            resolve(result)
        }.startAndSubscribe(completion)
    }
}
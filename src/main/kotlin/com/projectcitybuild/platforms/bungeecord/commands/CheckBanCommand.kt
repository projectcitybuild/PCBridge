package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.modules.bans.CheckBanStatusAction
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.modules.players.GetMojangPlayerAction
import com.projectcitybuild.modules.players.GetPlayerUUIDAction
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.Server
import java.util.*

class CheckBanCommand(
        private val proxyServer: ProxyServer,
        private val scheduler: SchedulerProvider,
        private val networkClients: NetworkClients,
): BungeecordCommand {

    override val label = "checkban"
    override val permission = "pcbridge.ban.checkban"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        if (input.args.isEmpty()) return CommandResult.INVALID_INPUT
        if (input.sender == null) throw Exception("Command sender is null")

        val targetPlayerName = input.args.first()

        input.sender.sendMessage(TextComponent("Searching for active bans for $targetPlayerName...").also {
            it.color = ChatColor.GRAY
        })

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
                    checkBanStatus(playerId = result.uuid) { result ->
                        scheduler.sync {
                            if (result is CheckBanStatusAction.Result.FAILED) {
                                when (result.reason) {
                                    CheckBanStatusAction.Failure.DESERIALIZE_FAILED -> {
                                        input.sender.sendMessage(TextComponent("Error: Bad response received from the ban server. Please contact an admin").also {
                                            it.color = ChatColor.RED
                                        })
                                    }
                                }
                            }
                            if (result is CheckBanStatusAction.Result.SUCCESS) {
                                if (result.ban == null) {
                                    input.sender.sendMessage("$targetPlayerName is not currently banned")
                                } else {
                                    input.sender.sendMessage("""
                                        #$targetPlayerName is currently banned.
                                        #---
                                        #Reason: ${result.ban.reason}
                                        #Date: ${result.ban.createdAt}
                                        #Expires: ${result.ban.expiresAt ?: "Never"}
                                    """.trimMargin("#"))
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

    private fun checkBanStatus(playerId: UUID, completion: (CheckBanStatusAction.Result) -> Unit) {
        scheduler.async<CheckBanStatusAction.Result> { resolve ->
            val action = CheckBanStatusAction(networkClients)
            val result = action.execute(
                    playerId = playerId
            )
            resolve(result)
        }.startAndSubscribe(completion)
    }

}
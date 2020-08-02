package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CheckBanStatusAction
import com.projectcitybuild.api.APIProvider
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.entities.LogLevel
import org.bukkit.ChatColor
import org.bukkit.Server
import org.bukkit.command.CommandSender
import java.util.*

class CheckBanCommand(
        private val environment: EnvironmentProvider,
        private val apiProvider: APIProvider
) : Commandable {

    override val label: String = "checkban"
    override val permission: String = "pcbridge.ban.checkban"

    override fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val targetPlayerName = input.args.first()

        input.sender.sendMessage("${ChatColor.GRAY}Searching for active bans for $targetPlayerName...")

        getOfflinePlayerUUID(server = input.sender.server, playerName = targetPlayerName) { uuid ->
            if (uuid == null) {
                environment.sync {
                    input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
                }
                return@getOfflinePlayerUUID
            }

            checkBanStatus(playerId = uuid) { result ->
                environment.sync {
                    if (result is CheckBanStatusAction.Result.FAILED) {
                        when (result.reason) {
                            CheckBanStatusAction.Failure.DESERIALIZE_FAILED -> {
                                input.sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
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
        return CommandResult.EXECUTED
    }

    private fun getOfflinePlayerUUID(server: Server, playerName: String, completion: (UUID?) -> Unit) {
        environment.async<UUID?> { resolve ->
            val uuid = server.getOfflinePlayer(
                    name = playerName,
                    environment = environment,
                    apiProvider = apiProvider
            )
            resolve(uuid)
        }.startAndSubscribe(completion)
    }

    private fun checkBanStatus(playerId: UUID, completion: (CheckBanStatusAction.Result) -> Unit) {
        environment.async<CheckBanStatusAction.Result> { resolve ->
            val action = CheckBanStatusAction(apiProvider)
            val result = action.execute(
                    playerId = playerId
            )
            resolve(result)
        }.startAndSubscribe(completion)
    }

}
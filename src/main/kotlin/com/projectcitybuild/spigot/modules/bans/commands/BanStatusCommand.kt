package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CheckBanStatusAction
import org.bukkit.Server
import org.bukkit.command.CommandSender
import java.util.*

class BanStatusCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "checkban"
    override val permission: String = "pcbridge.ban.checkban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        if (args.isEmpty()) return false

        val targetPlayerName = args.first()
        getOfflinePlayerUUID(server = sender.server, playerName = targetPlayerName) { uuid ->
            if (uuid == null) {
                environment.sync {
                    sender.sendMessage("Error: Failed to retrieve UUID of given player")
                }
                return@getOfflinePlayerUUID
            }

            checkBanStatus(playerId = uuid) { result ->
                environment.sync {
                    if (result is CheckBanStatusAction.Result.FAILED) {
                        when (result.reason) {
                            CheckBanStatusAction.Failure.DESERIALIZE_FAILED -> {
                                sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
                            }
                        }
                    }
                    if (result is CheckBanStatusAction.Result.SUCCESS) {
                        if (result.ban == null) {
                            sender.sendMessage("$targetPlayerName is not currently banned")
                        } else {
                            sender.sendMessage("""
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

        return true
    }

    private fun getOfflinePlayerUUID(server: Server, playerName: String, completion: (UUID?) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        environment.async<UUID?> { resolve ->
            val uuid = server.getOfflinePlayer(name = playerName, environment = environment)
            resolve(uuid)
        }.startAndSubscribe(completion)
    }

    private fun checkBanStatus(playerId: UUID, completion: (CheckBanStatusAction.Result) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        environment.async<CheckBanStatusAction.Result> { resolve ->
            val action = CheckBanStatusAction(environment)
            val result = action.execute(
                    playerId = playerId
            )
            resolve(result)
        }
    }

}
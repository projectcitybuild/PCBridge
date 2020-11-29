package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.modules.bans.CreateBanAction
import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
import org.bukkit.ChatColor
import org.bukkit.Server
import org.bukkit.entity.Player
import java.util.*

class BanCommand(
        private val environment: EnvironmentProvider,
        private val networkClients: NetworkClients
): Commandable {

    override val label: String = "ban"
    override val permission: String = "pcbridge.ban.ban"

    override fun execute(input: CommandInput): CommandResult {
        if (!input.hasArguments) return CommandResult.INVALID_INPUT

        val staffPlayer = if(input.isConsole) null else input.sender as Player
        val reason = input.args.joinWithWhitespaces(1 until input.args.size)
        val targetPlayerName = input.args.first()

        getOfflinePlayerUUID(server = input.sender.server, playerName = targetPlayerName) { uuid ->
            if (uuid == null) {
                environment.sync {
                    input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
                }
                return@getOfflinePlayerUUID
            }

            createBan(playerId = uuid, playerName = targetPlayerName, staffId = staffPlayer?.uniqueId, reason = reason) { result ->
                environment.sync {
                    when (result) {
                        is CreateBanAction.Result.FAILED -> {
                            val message = when (result.reason) {
                                CreateBanAction.Failure.PLAYER_ALREADY_BANNED -> "${input.args.first()} is already banned"
                                CreateBanAction.Failure.BAD_REQUEST -> "Bad request sent to the ban server. Please contact an administrator to have this fixed"
                                CreateBanAction.Failure.DESERIALIZE_FAILED -> "Error: Bad response received from the ban server. Please contact an admin"
                            }
                            input.sender.sendMessage(message)
                        }

                        is CreateBanAction.Result.SUCCESS -> {
                            input.sender.server.broadcast("${ChatColor.GRAY}${input.args.first()} has been banned by ${input.sender.name}: ${reason ?: "No reason given"}", "*")

                            val player = input.sender.server.onlinePlayers.first { player ->
                                player.name.toLowerCase() == targetPlayerName.toLowerCase()
                            }
                            player?.kickPlayer("You have been banned")
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
                    networkClients = networkClients
            )
            resolve(uuid)
        }.startAndSubscribe(completion)
    }

    private fun createBan(playerId: UUID, playerName: String, staffId: UUID?, reason: String?, completion: (CreateBanAction.Result) -> Unit) {
        environment.async<CreateBanAction.Result> { resolve ->
            val action = CreateBanAction(environment, networkClients)
            val result = action.execute(playerId, playerName, staffId, reason)
            resolve(result)
        }.startAndSubscribe(completion)
    }
}
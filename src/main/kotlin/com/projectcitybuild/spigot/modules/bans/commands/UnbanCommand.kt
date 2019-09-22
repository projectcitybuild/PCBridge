package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CreateUnbanAction
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class UnbanCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        if (args.isEmpty()) return false

        val targetPlayerName = args.first()
        val staffPlayer = if (isConsole) null else sender as Player

        getOfflinePlayerUUID(server = sender.server, playerName = targetPlayerName) { uuid ->
            if (uuid == null) {
                environment.sync {
                    sender.sendMessage("Error: Failed to retrieve UUID of given player")
                }
                return@getOfflinePlayerUUID
            }

            createUnban(playerId = uuid, staffId = staffPlayer?.uniqueId) { result ->
                environment.sync {
                    if (result is CreateUnbanAction.Result.FAILED) {
                        val message = when (result.reason) {
                            CreateUnbanAction.Failure.PLAYER_NOT_BANNED -> "${args.first()} is not currently banned"
                            CreateUnbanAction.Failure.BAD_REQUEST -> "Bad request sent to the ban server. Please contact an administrator to have this fixed"
                            CreateUnbanAction.Failure.DESERIALIZE_FAILED -> "Error: Bad response received from the ban server. Please contact an admin"
                            else -> "Error: An unexpected error has occurred"
                        }
                        sender.sendMessage(message)
                    }
                    if (result is CreateUnbanAction.Result.SUCCESS) {
                        sender.server.broadcast("${args.first()} has been unbanned", "*")
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

    private fun createUnban(playerId: UUID, staffId: UUID?, completion: (CreateUnbanAction.Result) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        environment.async<CreateUnbanAction.Result> { resolve ->
            val action = CreateUnbanAction(environment)
            val result = action.execute(
                    playerId = playerId,
                    staffId = staffId
            )
            resolve(result)
        }.startAndSubscribe(completion)
    }

}
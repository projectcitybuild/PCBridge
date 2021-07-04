package com.projectcitybuild.platforms.spigot.commands

//import com.projectcitybuild.core.contracts.Commandable
//import com.projectcitybuild.core.contracts.EnvironmentProvider
//import com.projectcitybuild.modules.bans.CreateUnbanAction
//import com.projectcitybuild.core.network.NetworkClients
//import com.projectcitybuild.core.contracts.CommandResult
//import com.projectcitybuild.core.entities.CommandInput
//import com.projectcitybuild.platforms.spigot.extensions.getOfflinePlayer
//import org.bukkit.Server
//import org.bukkit.entity.Player
//import java.util.*
//
//class UnbanCommand(
//        private val environment: EnvironmentProvider,
//        private val networkClients: NetworkClients
//): Commandable {
//
//    override val label: String = "unban"
//    override val permission: String = "pcbridge.ban.unban"
//
//    override fun execute(input: CommandInput): CommandResult {
//        if (!input.hasArguments) return CommandResult.INVALID_INPUT
//
//        val targetPlayerName = input.args.first()
//        val staffPlayer = if (input.isConsole) null else input.sender as Player
//
//        getOfflinePlayerUUID(server = input.sender.server, playerName = targetPlayerName) { uuid ->
//            if (uuid == null) {
//                environment.sync {
//                    input.sender.sendMessage("Error: Failed to retrieve UUID of given player")
//                }
//                return@getOfflinePlayerUUID
//            }
//
//            createUnban(playerId = uuid, staffId = staffPlayer?.uniqueId) { result ->
//                environment.sync {
//                    if (result is CreateUnbanAction.Result.FAILED) {
//                        val message = when (result.reason) {
//                            CreateUnbanAction.Failure.PLAYER_NOT_BANNED -> "${input.args.first()} is not currently banned"
//                            CreateUnbanAction.Failure.BAD_REQUEST -> "Bad request sent to the ban server. Please contact an administrator to have this fixed"
//                            CreateUnbanAction.Failure.DESERIALIZE_FAILED -> "Error: Bad response received from the ban server. Please contact an admin"
//                        }
//                        input.sender.sendMessage(message)
//                    }
//                    if (result is CreateUnbanAction.Result.SUCCESS) {
//                        input.sender.server.broadcast("${input.args.first()} has been unbanned", "*")
//                    }
//                }
//            }
//        }
//        return CommandResult.EXECUTED
//    }
//
//    private fun getOfflinePlayerUUID(server: Server, playerName: String, completion: (UUID?) -> Unit) {
//        environment.async<UUID?> { resolve ->
//            val uuid = server.getOfflinePlayer(
//                    name = playerName,
//                    environment = environment,
//                    networkClients = networkClients
//            )
//            resolve(uuid)
//        }.startAndSubscribe(completion)
//    }
//
//    private fun createUnban(playerId: UUID, staffId: UUID?, completion: (CreateUnbanAction.Result) -> Unit) {
//        environment.async<CreateUnbanAction.Result> { resolve ->
//            val action = CreateUnbanAction(networkClients)
//            val result = action.execute(
//                    playerId = playerId,
//                    staffId = staffId
//            )
//            resolve(result)
//        }.startAndSubscribe(completion)
//    }
//
//}
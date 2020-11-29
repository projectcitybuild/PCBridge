package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.network.NetworkClients
import com.projectcitybuild.core.contracts.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.AuthPlayerGroups
import com.projectcitybuild.core.entities.models.AuthURL
import com.projectcitybuild.modules.ranks.RankMapper
import net.luckperms.api.node.NodeType
import net.luckperms.api.node.types.InheritanceNode
import org.bukkit.entity.Player
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class SyncCommand(
        private val environment: EnvironmentProvider,
        private val networkClients: NetworkClients
): Commandable {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override fun execute(input: CommandInput): CommandResult {
        if (input.sender !is Player) {
            input.sender.sendMessage("Console cannot use this command")
            return CommandResult.EXECUTED
        }

        if (!input.hasArguments) {
            return beginSyncFlow(input.sender, environment, networkClients)
        }
        if (input.args.size == 1 && input.args[0] == "finish") {
            return endSyncFlow(input.sender, environment)
        }
        return CommandResult.INVALID_INPUT
    }

    private fun beginSyncFlow(sender: Player, environment: EnvironmentProvider, networkClients: NetworkClients): CommandResult {
        getVerificationLink(playerId = sender.uniqueId) { response ->
            val json = response.body()

            // TODO: handle error serialization in APIClient...
            if (!response.isSuccessful) {
                val annotation = object : Annotation {}
                val converter = networkClients.pcb.instance
                        .responseBodyConverter<ApiResponse<AuthURL>>(ApiResponse::class.java, arrayOf(annotation))

                val body = response.errorBody() ?: throw Exception("Error body deserialization failed")
                val model = converter.convert(body)

                environment.sync {
                    if (model?.error?.id == "already_authenticated") {
                        sender.sendMessage("Error: You have already linked your account")
                    } else {
                        sender.sendMessage("Failed to fetch verification URL: ${model?.error?.detail}")
                    }
                }
                return@getVerificationLink
            }

            environment.sync {
                if (json?.error != null) {
                    sender.sendMessage("Failed to fetch verification URL: ${json.error.detail}")
                    return@sync
                }
                if (json?.data?.url == null) {
                    sender.sendMessage("Server failed to generate verification URL. Please try again later")
                    return@sync
                }
                sender.sendMessage("To link your account, please click the link and login if required:§9 ${json.data.url}")
            }
        }
        return CommandResult.EXECUTED
    }

    private fun endSyncFlow(sender: Player, environment: EnvironmentProvider): CommandResult {
        val permissions = environment.permissions ?: throw Exception("Permission plugin is null")

        getPlayerGroups(playerId = sender.uniqueId) { result ->
            environment.sync {
                val json = result.body()
                if (json?.error != null) {
                    sender.sendMessage("Sync failed: Trouble communicating with the authentication server")
                    return@sync
                }

                val lpUser = permissions.userManager.getUser(sender.uniqueId)
                if (lpUser == null) {
                    sender.sendMessage("Sync failed: Could not load user from permission system. Please contact a staff member")
                    throw Exception("Could not load user from LuckPerms")
                }

                // Remove all groups from the player before syncing
                lpUser.nodes.stream()
                        .filter(NodeType.INHERITANCE::matches)
                        .map(NodeType.INHERITANCE::cast)
                        .collect(Collectors.toSet())
                        .forEach { groupNode ->
                            lpUser.data().remove(groupNode)
                        }

                if (json?.data == null) {
                    val groupNode = InheritanceNode.builder("guest").build()
                    lpUser.data().add(groupNode)

                    sender.sendMessage("No account found: Set to Guest")
                    return@sync
                }

                val permissionGroups = RankMapper.mapGroupsToPermissionGroups(json.data.groups)
                permissionGroups.forEach { group ->
                    val groupNode = InheritanceNode.builder(group).build()
                    if (!lpUser.nodes.contains(groupNode)) {
                        lpUser.data().add(groupNode)
                    }
                }

                // Just in case, assign to Guest if no groups available (shouldn't happen though)
                if (permissionGroups.isEmpty()) {
                    val groupNode = InheritanceNode.builder("guest").build()
                    lpUser.data().add(groupNode)
                }

                permissions.userManager.saveUser(lpUser)

                sender.sendMessage("Account successfully linked. Your rank will be automatically synchronized with the PCB network")
            }
        }
        return CommandResult.EXECUTED
    }

    private fun getVerificationLink(playerId: UUID, completion: (Response<ApiResponse<AuthURL>>) -> Unit) {
        val authApi = networkClients.pcb.authApi

        environment.async<Response<ApiResponse<AuthURL>>> { resolve ->
            val request = authApi.getVerificationUrl(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }

    private fun getPlayerGroups(playerId: UUID, completion: (Response<ApiResponse<AuthPlayerGroups>>) -> Unit) {
        val authApi = networkClients.pcb.authApi

        environment.async<Response<ApiResponse<AuthPlayerGroups>>> { resolve ->
            val request = authApi.getUserGroups(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }
}

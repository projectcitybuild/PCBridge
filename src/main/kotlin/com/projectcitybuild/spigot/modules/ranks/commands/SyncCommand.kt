package com.projectcitybuild.spigot.modules.ranks.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.AuthPlayerGroups
import com.projectcitybuild.entities.models.AuthURL
import com.projectcitybuild.spigot.modules.ranks.RankMapper
import me.lucko.luckperms.api.Node
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import retrofit2.Response
import java.util.*
import java.util.stream.Collectors

class SyncCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")

        if (sender !is Player) {
            sender.sendMessage("Console cannot use this command")
            return true
        }

        if (args.isEmpty()) {
            return beginSyncFlow(sender, environment)
        }
        if (args.size == 1 && args[0] == "finish") {
            return endSyncFlow(sender, environment)
        }
        return false
    }

    private fun beginSyncFlow(sender: Player, environment: EnvironmentProvider): Boolean {
        getVerificationLink(playerId = sender.uniqueId) { response ->
            val json = response.body()

            // TODO: handle error serialization in APIClient...
            if (!response.isSuccessful) {
                val annotation = object : Annotation {}
                val converter = environment.apiClient.instance
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

        return true
    }

    private fun endSyncFlow(sender: Player, environment: EnvironmentProvider): Boolean {
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
                lpUser.allNodes.stream()
                        .filter(Node::isGroupNode)
                        .collect(Collectors.toSet())
                        .forEach { groupNode ->
                            lpUser.unsetPermission(groupNode)
                        }

                if (json?.data == null) {
                    sender.sendMessage("No account found: Set to Guest")
                    return@sync
                }

                val permissionGroups = RankMapper.mapGroupsToPermissionGroups(json.data.groups)
                permissionGroups.forEach { group ->
                    val groupNode = permissions.nodeFactory.makeGroupNode(group).build()
                    if (!lpUser.hasPermission(groupNode).asBoolean()) {
                        lpUser.setPermission(groupNode)
                    }
                }

                permissions.userManager.saveUser(lpUser)

                sender.sendMessage("Account successfully linked. Your rank will be automatically synchronized with the PCB network")
            }
        }

        return true
    }

    private fun getVerificationLink(playerId: UUID, completion: (Response<ApiResponse<AuthURL>>) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val authApi = environment.apiClient.authApi

        environment.async<Response<ApiResponse<AuthURL>>> { resolve ->
            val request = authApi.getVerificationUrl(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }

    private fun getPlayerGroups(playerId: UUID, completion: (Response<ApiResponse<AuthPlayerGroups>>) -> Unit) {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val authApi = environment.apiClient.authApi

        environment.async<Response<ApiResponse<AuthPlayerGroups>>> { resolve ->
            val request = authApi.getUserGroups(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }
}

package com.projectcitybuild.spigot.modules.ranks.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.AuthPlayerGroups
import com.projectcitybuild.entities.models.AuthURL
import com.projectcitybuild.entities.models.VerificationUrl
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import retrofit2.Response
import java.util.*

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
        if (args.size == 1 && args[1] == "finish") {
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
                        .responseBodyConverter<ApiResponse<VerificationUrl>>(ApiResponse::class.java, arrayOf(annotation))
                val model = converter.convert(response.errorBody())

                environment.sync {
                    if (model.error.id == "already_authenticated") {
                        sender.sendMessage("Error: You have already linked your account")
                    } else {
                        sender.sendMessage("Failed to fetch verification URL: ${model.error.detail}")
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
                sender.sendMessage("To link your account, please ${json.data.url} and login if required:")
            }
        }
//            sender.sendMessage(
//                    "Please click the below URL to link this account with your PCB account:\n" +
//                            "{text:\"" + "[Link Account]" + "\",clickEvent:{action:open_url,value:\"" + data.url + "\"}}"
//            )

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

                // Remove all groups from the player before syncing
                permissions.getPlayerGroups(sender).forEach { group ->
                    permissions.playerRemoveGroup(sender, group)
                }

                if (json?.data == null) {
                    sender.sendMessage("No account found: Set to Guest")
                    return@sync
                }

                json.data.groups.forEach { group ->
                    when (group.name) {
                        "donator" -> {
                            if (!permissions.playerInGroup(sender, "Donator")) {
                                permissions.playerAddGroup(null, sender, "Donator")
                            }
                        }
                        "trusted" -> {
                            if (!permissions.playerInGroup(sender, "Trusted")) {
                                permissions.playerAddGroup(null, sender, "Trusted")
                            }
                        }
                        "moderator" -> {
                            if (!permissions.playerInGroup(sender, "Mod")) {
                                permissions.playerAddGroup(null, sender, "Mod")
                            }
                        }
                        "operator" -> {
                            if (!permissions.playerInGroup(sender, "OP")) {
                                permissions.playerAddGroup(null, sender, "OP")
                            }
                        }
                        "senior operator" -> {
                            if (!permissions.playerInGroup(sender, "SOP")) {
                                permissions.playerAddGroup(null, sender, "SOP")
                            }
                        }
                        "administrator" -> {
                            if (!permissions.playerInGroup(sender, "Admin")) {
                                permissions.playerAddGroup(null, sender, "Admin")
                            }
                        }
                    }
                }

                if (json.data.groups.isEmpty()) {
                    permissions.playerAddGroup(null, sender, "Member")
                }

                sender.sendMessage("Sync Complete")
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
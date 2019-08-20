package com.projectcitybuild.spigot.modules.ranks.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.entities.models.ApiResponse
import com.projectcitybuild.entities.models.VerificationUrl
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LoginCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (sender !is Player) {
            sender.sendMessage("Console cannot use this command")
            return true
        }
        if (args.size > 1) return false

        GlobalScope.launch(BukkitDispatcher(plugin, async = true)) {
            val rankApi = environment.apiClient.rankApi

            val request = rankApi.fetchVerificationURL(uuid = sender.uniqueId.toString())
            val response = request.execute()
            val json = response.body()

            // TODO: handle error serialization in APIClient...
            if (!response.isSuccessful) {
                val annotation = object : Annotation {}
                val converter = environment.apiClient.instance
                        .responseBodyConverter<ApiResponse<VerificationUrl>>(ApiResponse::class.java, arrayOf(annotation))

                val model = converter.convert(response.errorBody())
                if (model.error?.id == "already_authenticated") {
                    sender.sendMessage("Error: You have already linked your account")
                    return@launch
                } else {
                    sender.sendMessage("Failed to fetch verification URL: ${model.error?.detail}")
                    return@launch
                }
            }
            if (json?.error != null) {
                sender.sendMessage("Failed to fetch verification URL: ${json.error.detail}")
                return@launch
            }

            if (json?.data?.url == null) {
                sender.sendMessage("Server failed to generate verification URL. Please try again later")
                return@launch
            }

            sender.sendMessage("To link your account, please ${json.data.url} and login if required:")

//            val permissions = environment.permissions
//            if (permissions == null) {
//                sender.sendMessage("Sync failed: Permission plugin unavailable. Please contact a staff member")
//                return@launch
//            }
//
//            // remove all groups from the player before syncing
//            permissions.getPlayerGroups(sender).forEach { group ->
//                permissions.playerRemoveGroup(sender, group)
//            }
//
//            if (json?.data == null) {
//                sender.sendMessage("No account found: Set to Guest")
//                return@launch
//            }
//
//            json.data.groups.forEach { group ->
//                when (group.name) {
//                    "donator" -> {
//                        if (!permissions.playerInGroup(sender, "Donator")) {
//                            permissions.playerAddGroup(null, sender, "Donator")
//                        }
//                    }
//                    "trusted" -> {
//                        if (!permissions.playerInGroup(sender, "Trusted")) {
//                            permissions.playerAddGroup(null, sender, "Trusted")
//                        }
//                    }
//                    "moderator" -> {
//                        if (!permissions.playerInGroup(sender, "Mod")) {
//                            permissions.playerAddGroup(null, sender, "Mod")
//                        }
//                    }
//                    "operator" -> {
//                        if (!permissions.playerInGroup(sender, "OP")) {
//                            permissions.playerAddGroup(null, sender, "OP")
//                        }
//                    }
//                    "senior operator" -> {
//                        if (!permissions.playerInGroup(sender, "SOP")) {
//                            permissions.playerAddGroup(null, sender, "SOP")
//                        }
//                    }
//                    "administrator" -> {
//                        if (!permissions.playerInGroup(sender, "Admin")) {
//                            permissions.playerAddGroup(null, sender, "Admin")
//                        }
//                    }
//                }
//            }
//
//            if (json.data.groups.isEmpty()) {
//                permissions.playerAddGroup(null, sender, "Member")
//            }
//
//            sender.sendMessage("Sync Complete")
//        }
        }
        return true
    }
}
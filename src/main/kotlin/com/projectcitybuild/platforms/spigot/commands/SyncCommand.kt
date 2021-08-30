package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.entities.models.ApiResponse
import com.projectcitybuild.core.entities.models.AuthURL
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.GetGroupsForUUIDAction
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import retrofit2.Response
import java.util.*

class SyncCommand(
        private val scheduler: SchedulerProvider,
        private val permissionsManager: PermissionsManager,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider
): Commandable {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override fun execute(input: CommandInput): CommandResult {
        if (input.sender !is Player) {
            input.sender.sendMessage("Console cannot use this command")
            return CommandResult.EXECUTED
        }

        if (!input.hasArguments) {
            return beginSyncFlow(input.sender)
        }
        if (input.args.size == 1 && input.args[0] == "finish") {
            return endSyncFlow(input.sender)
        }
        return CommandResult.INVALID_INPUT
    }

    private fun beginSyncFlow(sender: Player): CommandResult {
        getVerificationLink(playerId = sender.uniqueId) { response ->
            val json = response.body()

            // TODO: handle error serialization in APIClient...
            if (!response.isSuccessful) {
                val annotation = object : Annotation {}
                val converter = apiRequestFactory.pcb.instance
                        .responseBodyConverter<ApiResponse<AuthURL>>(ApiResponse::class.java, arrayOf(annotation))

                val body = response.errorBody() ?: throw Exception("Error body deserialization failed")
                val model = converter.convert(body)

                scheduler.sync {
                    if (model?.error?.id == "already_authenticated") {
                        sender.sendMessage("Error: You have already linked your account")
                    } else {
                        sender.sendMessage("Failed to fetch verification URL: ${model?.error?.detail}")
                    }
                }
                return@getVerificationLink
            }

            scheduler.sync {
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

    private fun endSyncFlow(player: Player): CommandResult {
        GetGroupsForUUIDAction(apiRequestFactory, apiClient).execute(
            playerId = player.uniqueId
        ) { result ->
            val groupsForPlayer = if (result is Success) result.value else listOf()
            if (groupsForPlayer.isEmpty()) {
                player.sendMessage("${ChatColor.RED}Sync failed. Did you finish registering your account?")
                return@execute
            }

            scheduler.sync {
                val user = permissionsManager.getUser(player.uniqueId)
                if (user == null) {
                    logger.warning("Could not load user from permissions manager (uuid: ${player.uniqueId})")
                    player.sendMessage("Sync failed: Could not load user from permission system. Please contact a staff member")
                    return@sync
                }

                user.removeAllGroups()

                if (groupsForPlayer.isEmpty()) {
                    // TODO: retrieve this from config instead
                    val guestGroup = permissionsManager.getGroup("guest")
                    user.addGroup(guestGroup)
                    permissionsManager.saveChanges(user)
                    player.sendMessage("No account found: Set to Guest")
                    return@sync
                }

                groupsForPlayer.forEach { apiGroup ->
                    if (apiGroup.minecraftName != null) {
                        logger.info("Assigning to ${apiGroup.minecraftName} group")
                        val group = permissionsManager.getGroup(apiGroup.minecraftName)
                        user.addGroup(group)
                    } else {
                        logger.info("No group found for ${apiGroup.name}. Skipping...")
                    }
                }
                permissionsManager.saveChanges(user)
                player.sendMessage("${ChatColor.GREEN}Account successfully linked. Your rank will be automatically synchronized with the PCB network")
            }
        }
        return CommandResult.EXECUTED
    }

    private fun getVerificationLink(playerId: UUID, completion: (Response<ApiResponse<AuthURL>>) -> Unit) {
        val authApi = apiRequestFactory.pcb.authApi

        scheduler.async<Response<ApiResponse<AuthURL>>> { resolve ->
            val request = authApi.getVerificationUrl(uuid = playerId.toString())
            val response = request.execute()

            resolve(response)
        }.startAndSubscribe(completion)
    }
}

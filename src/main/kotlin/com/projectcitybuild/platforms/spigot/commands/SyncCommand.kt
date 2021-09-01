package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.LoggerProvider
import com.projectcitybuild.core.contracts.SchedulerProvider
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.environment.PermissionsManager
import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player

class SyncCommand(
        private val scheduler: SchedulerProvider,
        private val permissionsManager: PermissionsManager,
        private val apiRequestFactory: APIRequestFactory,
        private val apiClient: APIClient,
        private val logger: LoggerProvider,
        private val syncPlayerGroupAction: SyncPlayerGroupAction
): Commandable {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.sender !is Player) {
            input.sender.sendMessage("Console cannot use this command")
            return CommandResult.EXECUTED
        }

//        if (!input.hasArguments) {
//            return beginSyncFlow(input.sender)
//        }
        if (input.args.size == 1 && input.args[0] == "finish") {
            return endSyncFlow(input.sender)
        }
        return CommandResult.INVALID_INPUT
    }

//    private fun beginSyncFlow(sender: Player): CommandResult {
//        getVerificationLink(playerId = sender.uniqueId) { response ->
//            val json = response.body()
//
//            // TODO: handle error serialization in APIClient...
//            if (!response.isSuccessful) {
//                val annotation = object : Annotation {}
//                val converter = apiRequestFactory.pcb.instance
//                        .responseBodyConverter<ApiResponse<AuthURL>>(ApiResponse::class.java, arrayOf(annotation))
//
//                val body = respones.errorBody() ?: throw Exception("Error body deserialization failed")
//                val model = converter.convert(body)
//
//                scheduler.sync {
//                    if (model?.error?.id == "already_authenticated") {
//                        sender.sendMessage("Error: You have already linked your account")
//                    } else {
//                        sender.sendMessage("Failed to fetch verification URL: ${model?.error?.detail}")
//                    }
//                }
//                return@getVerificationLink
//            }
//
//            scheduler.sync {
//                if (json?.error != null) {
//                    sender.sendMessage("Failed to fetch verification URL: ${json.error.detail}")
//                    return@sync
//                }
//                if (json?.data?.url == null) {
//                    sender.sendMessage("Server failed to generate verification URL. Please try again later")
//                    return@sync
//                }
//                sender.sendMessage("To link your account, please click the link and login if required:§9 ${json.data.url}")
//            }
//        }
//        return CommandResult.EXECUTED
//    }
//
    private suspend fun endSyncFlow(player: Player): CommandResult {
        val result = syncPlayerGroupAction.execute(player.uniqueId)

        when (result) {
            is Success -> player.sendMessage("${ChatColor.GREEN}Account successfully linked. Your rank will be automatically synchronized with the PCB network")
            is Failure -> {
                when (result.reason) {
                    is SyncPlayerGroupAction.FailReason.AccountNotLinked ->
                        player.sendMessage("${ChatColor.RED}Sync failed. Did you finish registering your account?")

                    else -> player.sendMessage("${ChatColor.RED}Failed to contact auth server. Please contact staff")
                }
            }
        }
        return CommandResult.EXECUTED
    }
//
//    private fun getVerificationLink(playerId: UUID, completion: (Response<ApiResponse<AuthURL>>) -> Unit) {
//        val authApi = apiRequestFactory.pcb.authApi
//
//        scheduler.async<Response<ApiResponse<AuthURL>>> { resolve ->
//            val request = authApi.getVerificationUrl(uuid = playerId.toString())
//            val response = request.execute()
//
//            resolve(response)
//        }.startAndSubscribe(completion)
//    }
}

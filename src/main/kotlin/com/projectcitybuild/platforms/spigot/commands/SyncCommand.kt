package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.entities.CommandInput
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import com.projectcitybuild.platforms.spigot.send
import org.bukkit.entity.Player

class SyncCommand(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val syncPlayerGroupAction: SyncPlayerGroupAction
): Commandable {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"

    override suspend fun execute(input: CommandInput): CommandResult {
        if (input.sender !is Player) {
            input.sender.send().error("Console cannot use this command")
            return CommandResult.EXECUTED
        }

        if (!input.hasArguments) {
            return generateVerificationURL(input.sender)
        }
        if (input.args.size == 1 && input.args[0] == "finish") {
            return syncGroups(input.sender)
        }
        return CommandResult.INVALID_INPUT
    }

    private suspend fun generateVerificationURL(player: Player): CommandResult {
        try {
            val authApi = apiRequestFactory.pcb.authApi
            val response = apiClient.execute { authApi.getVerificationUrl(uuid = player.uniqueId.toString()) }

            if (response.data == null) {
                player.send().error("Failed to generate verification URL: No URL received from server")
            } else {
                player.sendMessage("To link your account, please click the link and login if required:§9 ${response.data.url}")
            }

        } catch (throwable: APIClient.HTTPError) {
            if (throwable.errorBody?.id == "already_authenticated") {
                syncGroups(player)
            } else {
                player.send().error("Failed to generate verification URL: ${throwable.errorBody?.detail}")
            }
            return CommandResult.EXECUTED

        } catch (throwable: Throwable) {
            player.send().error(throwable.message ?: "An unknown error occurred")
            return CommandResult.EXECUTED
        }


        return CommandResult.EXECUTED
    }

    private suspend fun syncGroups(player: Player): CommandResult {
        runCatching {
            syncPlayerGroupAction.execute(player.uniqueId)
        }.onFailure { throwable ->
            player.send().error(
                if (throwable is SyncPlayerGroupAction.AccountNotLinkedException)
                    "Sync failed. Did you finish registering your account?"
                else
                    throwable.message ?: "An unknown error occurred"
            )
            return CommandResult.EXECUTED
        }

        player.send().success("Account linked! Your rank will be automatically synchronized with the PCB network")

        return CommandResult.EXECUTED
    }
}

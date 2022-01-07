package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.modules.ranks.SyncPlayerGroupService
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.send
import net.md_5.bungee.api.connection.ProxiedPlayer

class SyncCommand(
    private val apiRequestFactory: APIRequestFactory,
    private val apiClient: APIClient,
    private val syncPlayerGroupService: SyncPlayerGroupService
): BungeecordCommand {

    override val label: String = "sync"
    override val permission: String = "pcbridge.sync.login"
    override val usageHelp = "/sync [finish]"

    override suspend fun execute(input: BungeecordCommandInput) {
        if (input.isConsoleSender) {
            input.sender.send().error("Console cannot use this command")
            return
        }
        if (input.args.isEmpty()) {
            generateVerificationURL(input.sender as ProxiedPlayer)
        }
        else if (input.args.size == 1 && input.args.first() == "finish") {
            syncGroups(input.sender as ProxiedPlayer)
        }
        else {
            input.sender.send().invalidCommandInput(this)
        }
    }

    private suspend fun generateVerificationURL(player: ProxiedPlayer) {
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

        } catch (throwable: Throwable) {
            player.send().error(throwable.message ?: "An unknown error occurred")
        }
    }

    private suspend fun syncGroups(player: ProxiedPlayer) {
        runCatching {
            syncPlayerGroupService.execute(player.uniqueId)
        }.onFailure { throwable ->
            player.send().error(
                if (throwable is SyncPlayerGroupService.AccountNotLinkedException)
                    "Sync failed. Did you finish registering your account?"
                else
                    throwable.message ?: "An unknown error occurred"
            )
            return
        }
        player.send().success("Account linked! Your rank will be automatically synchronized with the PCB network")
    }
}

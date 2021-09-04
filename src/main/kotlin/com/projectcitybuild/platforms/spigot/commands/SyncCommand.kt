package com.projectcitybuild.platforms.spigot.commands

import com.projectcitybuild.core.network.APIRequestFactory
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.entities.CommandInput
import com.projectcitybuild.core.entities.Failure
import com.projectcitybuild.core.entities.Success
import com.projectcitybuild.core.network.APIClient
import com.projectcitybuild.core.network.APIResult
import com.projectcitybuild.modules.ranks.SyncPlayerGroupAction
import net.md_5.bungee.api.ChatColor
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

    private suspend fun beginSyncFlow(sender: Player): CommandResult {
        val authApi = apiRequestFactory.pcb.authApi
        val response = apiClient.execute { authApi.getVerificationUrl(uuid = sender.uniqueId.toString()) }

        when (response) {
            is APIResult.HTTPError -> {
                val error = response.error
                if (error?.id == "already_authenticated") {
                    sender.sendMessage("Error: You have already linked your account")
                } else {
                    sender.sendMessage("Error: Failed to fetch verification URL: ${error?.detail}")
                }
            }
            is APIResult.NetworkError -> {
                sender.sendMessage("Error: Failed to contact auth server. Please try again later")
            }
            is APIResult.Success -> {
                if (response.value.data == null) {
                    sender.sendMessage("Error: Failed to fetch verification URL")
                } else {
                    sender.sendMessage("To link your account, please click the link and login if required:§9 ${response.value.data?.url}")
                }
            }
        }

        return CommandResult.EXECUTED
    }

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
}

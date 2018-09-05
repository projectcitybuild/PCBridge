package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.entities.LogLevel
import com.projectcitybuild.entities.requests.GameBanStatusRequest
import org.bukkit.command.CommandSender

class BanStatusCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "status"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val banApi = environment.apiClient().banApi

        val request = GameBanStatusRequest(playerId = "bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f", playerType = "minecraft_uuid")
        val response = banApi.requestStatus(request).execute()
        val status = response.body()

        if (status != null) {
            environment.log(LogLevel.INFO, status.reason)
        } else {
            environment.log(LogLevel.INFO, "Status is null")
        }
        return true
    }
}
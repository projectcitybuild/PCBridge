package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import org.bukkit.command.CommandSender
import java.util.*

class BanStatusCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "status"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val banApi = environment.apiClient.banApi

        val request = banApi.requestStatus(playerId = "bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f", playerType = "minecraft_uuid")
        val response = request.execute()
        val json = response.body()

        if (json?.data == null) {
            sender.sendMessage("User is not currently banned")
            return true
        }

        val status = json.data
        if (!status.isActive) {
            sender.sendMessage("User is not currently banned")
            return true
        }

        if (status.expiresAt != null && status.expiresAt <= Date().time) {
            sender.sendMessage("User is not currently banned")
            return true
        }

        return true
    }
}
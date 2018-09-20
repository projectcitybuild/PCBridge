package com.projectcitybuild.spigot.modules.bans.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.spigot.modules.bans.actions.CheckBanStatusAction
import kotlinx.coroutines.experimental.launch
import org.bukkit.command.CommandSender
import java.util.*

class BanStatusCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "status"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        launch(BukkitDispatcher(plugin, async = true)) {
            val action = CheckBanStatusAction(environment)
            val result = action.execute(
                    playerId = UUID.fromString("bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f")
            )
            if (result is CheckBanStatusAction.Result.FAILED) {
                when (result.reason) {
                    CheckBanStatusAction.Failure.DESERIALIZE_FAILED -> {
                        sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
                    }
                }
            }
            if (result is CheckBanStatusAction.Result.SUCCESS) {
                if (result.ban == null) {
                    sender.sendMessage("Player is not currently banned")
                } else {
                    sender.sendMessage(result.ban.reason)
                }
            }
        }
        return true
    }
}
package com.projectcitybuild.spigot.modules.bans.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CheckBanStatusAction
import kotlinx.coroutines.experimental.launch
import org.bukkit.command.CommandSender

class BanStatusCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "status"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        launch(BukkitDispatcher(plugin, async = true)) {
            val targetPlayerName = args.first()
            val playerUUID = sender.server.getOfflinePlayer(name = targetPlayerName, environment = environment)
            if (playerUUID == null) {
                sender.sendMessage("Error: Failed to retrieve UUID of given player")
                return@launch
            }

            val action = CheckBanStatusAction(environment)
            val result = action.execute(
                    playerId = playerUUID
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
                    sender.sendMessage("""
                        $targetPlayerName is currently banned.
                        ---
                        Reason: ${result.ban.reason}
                        Date: ${result.ban.createdAt}
                        Expires: ${result.ban.expiresAt ?: "Never"}
                    """)
                }
            }
        }
        return true
    }
}
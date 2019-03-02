package com.projectcitybuild.spigot.modules.bans.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CreateUnbanAction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UnbanCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "unban"
    override val permission: String = "pcbridge.ban.unban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (args.isEmpty()) return false

        GlobalScope.launch(BukkitDispatcher(plugin, async = true)) {
            val targetPlayerName = args.first()
            val playerUUID = sender.server.getOfflinePlayer(name = targetPlayerName, environment = environment)
            if (playerUUID == null) {
                sender.sendMessage("Error: Failed to retrieve UUID of given player")
                return@launch
            }

            val staffPlayer = if (isConsole) null else sender as Player

            val action = CreateUnbanAction(environment)
            val result = action.execute(
                    playerId = playerUUID,
                    staffId = staffPlayer?.uniqueId
            )

            if (result is CreateUnbanAction.Result.FAILED) {
                when (result.reason) {
                    CreateUnbanAction.Failure.PLAYER_NOT_BANNED -> {
                        sender.sendMessage("${args.first()} is not currently banned")
                    }
                    else -> {
                        sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
                    }
                }
            }
            if (result is CreateUnbanAction.Result.SUCCESS) {
                sender.server.broadcast("${args.first()} has been unbanned", "*")
            }
        }

        return true
    }

}
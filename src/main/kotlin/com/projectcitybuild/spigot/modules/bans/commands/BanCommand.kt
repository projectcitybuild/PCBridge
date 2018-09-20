package com.projectcitybuild.spigot.modules.bans.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.core.extensions.toDashFormattedUUID
import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.spigot.modules.bans.actions.CreateBanAction
import com.projectcitybuild.spigot.modules.bans.actions.GetMojangPlayerAction
import kotlinx.coroutines.experimental.launch
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class BanCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "ban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (args.isEmpty()) return false

        val staffPlayer = if(isConsole) null else sender as Player
        val reason = if(args.size > 1) args.sliceArray(1..args.size).joinToString(separator = " ") else null

        launch(BukkitDispatcher(plugin, async = true)) {
            var targetUUID: UUID? = null
            val targetPlayerName = args.first()
            val targetPlayer = sender.server.getOnlinePlayer(name = targetPlayerName)
            if (targetPlayer != null) {
                targetUUID = targetPlayer.uniqueId
            } else {
                val mojangPlayerAction = GetMojangPlayerAction(environment)
                val result = mojangPlayerAction.execute(playerName = targetPlayerName)
                if (result is GetMojangPlayerAction.Result.FAILED) {
                    sender.sendMessage("That player does not exist in Mojang's database")
                    return@launch
                }
                if (result is GetMojangPlayerAction.Result.SUCCESS) {
                    targetUUID = UUID.fromString(result.player.uuid.toDashFormattedUUID())
                }
            }
            if (targetUUID == null) {
                sender.sendMessage("Error: Failed to retrieve UUID of given player")
                return@launch
            }

            val action = CreateBanAction(environment)
            val result = action.execute(
                    playerId = targetUUID,
                    playerName = targetPlayerName,
                    staffId = staffPlayer?.uniqueId,
                    reason = reason
            )
            if (result is CreateBanAction.Result.FAILED) {
                when (result.reason) {
                    CreateBanAction.Failure.PLAYER_ALREADY_BANNED -> {
                        sender.sendMessage("${args.first()} is already banned")
                    }
                    else -> {
                        sender.sendMessage("Error: Bad response received from the ban server. Please contact an admin")
                    }
                }
            }
            if (result is CreateBanAction.Result.SUCCESS) {
                sender.server.broadcast("${args.first()} has been banned", "*")
            }
        }

        return true
    }

}
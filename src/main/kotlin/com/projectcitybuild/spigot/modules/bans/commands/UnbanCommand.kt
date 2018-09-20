package com.projectcitybuild.spigot.modules.bans.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.spigot.extensions.getOnlinePlayer
import com.projectcitybuild.spigot.modules.bans.actions.CreateUnbanAction
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class UnbanCommand : Commandable {
    override var environment: Environment? = null
    override val label: String = "unban"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (args.isEmpty()) return false

        val targetPlayerName = args.first()
        val targetPlayer = sender.server.getOnlinePlayer(name = targetPlayerName)
        if (targetPlayer == null) {

        }

        val staffPlayer = if(isConsole) null else sender as Player

        val action = CreateUnbanAction(environment)
        val result = action.execute(
                playerId = UUID.fromString("bee2c0bb-2f5b-47ce-93f9-734b3d7fef5f"),
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

        return true
    }

}
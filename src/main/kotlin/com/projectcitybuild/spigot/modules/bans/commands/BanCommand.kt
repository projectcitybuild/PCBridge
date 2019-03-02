package com.projectcitybuild.spigot.modules.bans.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import com.projectcitybuild.spigot.extensions.getOfflinePlayer
import com.projectcitybuild.actions.CreateBanAction
import com.projectcitybuild.core.extensions.joinWithWhitespaces
import com.projectcitybuild.spigot.modules.bans.interactors.BanCommandInteractor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BanCommand: Commandable {

    override var environment: EnvironmentProvider? = null
    override val label: String = "ban"

    private var interactor: BanCommandInteractor? = null

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        // TODO: move instantiation out of this class
        interactor = BanCommandInteractor(sender = sender)

        val environment = environment
                ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin
                ?: throw Exception("Plugin has already been deallocated")

        if (args.isEmpty()) return false

        val staffPlayer = if(isConsole) null else sender as Player
        val reason = args.joinWithWhitespaces(1..args.size-1)

        GlobalScope.launch(BukkitDispatcher(plugin, async = true)) {
            val targetPlayerName = args.first()
            val playerUUID = sender.server.getOfflinePlayer(name = targetPlayerName, environment = environment)
            if (playerUUID == null) {
                interactor?.messageUUIDLookupFailed()
                return@launch
            }
            val action = CreateBanAction(environment)
            val result = action.execute(
                    playerId    = playerUUID,
                    playerName  = targetPlayerName,
                    staffId     = staffPlayer?.uniqueId,
                    reason      = reason
            )
            if (result is CreateBanAction.Result.FAILED) {
                when (result.reason) {
                    CreateBanAction.Failure.PLAYER_ALREADY_BANNED -> {
                        interactor?.messageAlreadyBanned(name = args.first())
                    }
                    else -> {
                        interactor?.messageBanFailed()
                    }
                }
            }
            if (result is CreateBanAction.Result.SUCCESS) {
                interactor?.broadcastPlayerBanned(name = args.first())
            }

            // kick the player regardless
            val player = sender.server.onlinePlayers.first { player ->
                player.name.toLowerCase() == targetPlayerName.toLowerCase()
            }
            player?.kickPlayer("You have been banned")
        }

        return true
    }

}
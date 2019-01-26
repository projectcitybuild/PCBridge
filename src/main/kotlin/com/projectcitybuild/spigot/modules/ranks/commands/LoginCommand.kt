package com.projectcitybuild.spigot.modules.ranks.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import kotlinx.coroutines.experimental.launch
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class LoginCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "login"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (sender !is Player) {
            sender.sendMessage("Console cannot use this command")
            return true
        }
        if (args.size < 2) return false

        launch(BukkitDispatcher(plugin, async = true)) {
            val rankApi = environment.apiClient.rankApi

            val request = rankApi.login(
                    email = args[0],
                    password = args[1]
            )
            val response = request.execute()
            val json = response.body()

            if (json?.error != null) {
                sender.sendMessage("Login failed: ${json.error.detail}")
                return@launch
            }
            if (json?.data == null) {
                sender.sendMessage("Login failed: Data unavailable")
                return@launch
            }
            if (!json.data.isActive) {
                sender.sendMessage("Cannot authenticate: Your PCB account is suspended")
                return@launch
            }

            val permissions = environment.permissions
            if (permissions == null) {
                sender.sendMessage("Sync failed: Permission plugin unavailable. Please contact a staff member")
                return@launch
            }
            if (permissions.playerInGroup(sender, "Member")) {
                sender.sendMessage("Your account is already up-to-date")
                return@launch
            }

            permissions.playerAddGroup(null, sender, "Member")
            sender.sendMessage("Sync complete: Your rank has been set to Member")
        }

        return true
    }
}
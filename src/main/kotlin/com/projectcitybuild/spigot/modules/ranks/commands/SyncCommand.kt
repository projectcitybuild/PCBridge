package com.projectcitybuild.spigot.modules.ranks.commands

import com.okkero.skedule.BukkitDispatcher
import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.EnvironmentProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SyncCommand : Commandable {
    override var environment: EnvironmentProvider? = null
    override val label: String = "sync"
    override val permission: String = "pcbridge.sync"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (sender !is Player) {
            sender.sendMessage("Console cannot use this command")
            return true
        }

        val authApi = environment.apiClient.authApi

        if (args.isEmpty()) {
            GlobalScope.launch(BukkitDispatcher(plugin, async = true)) {
                val request = authApi.store(uuid = sender.uniqueId.toString())
                val response = request.execute()
                val json = response.body()

                if (json?.error != null) {
                    sender.sendMessage("Sync failed: ${json.error.detail}")
                    return@launch
                }

                val data = json?.data
                if (data == null) {
                    sender.sendMessage("Sync failed: Account data not found. The server might be busy, please try again later")
                    return@launch
                }

                sender.sendMessage(
                        "Please click the below URL to link this account with your PCB account:\n" +
                        "{text:\"" + "[Link Account]" + "\",clickEvent:{action:open_url,value:\"" + data.url + "\"}}"
                )
            }

        } else {
            GlobalScope.launch(BukkitDispatcher(plugin, async = true)) {
                val request = authApi.show(uuid = sender.uniqueId.toString())
                val response = request.execute()
                val json = response.body()

                if (json?.error != null) {
                    sender.sendMessage("Sync failed: ${json.error.detail}")
                    return@launch
                }

                val data = json?.data
                if (data == null) {
                    sender.sendMessage("Sync failed: Account data not found. The server might be busy, please try again later")
                    return@launch
                }

                val permissions = environment.permissions
                if (permissions == null) {
                    sender.sendMessage("Sync failed: Permission plugin unavailable. Please contact a staff member")
                    return@launch
                }

                var isDonator = false
                data.groups.forEach { group ->
                    if (group.name == "donator") {
                        isDonator = true
                    }
                }

                if (permissions.playerInGroup(sender, "Member")) {
                    sender.sendMessage("Your account is already up-to-date")
                    return@launch
                }

                permissions.playerAddGroup(null, sender, "Member")
                sender.sendMessage("Sync complete: Your rank has been updated")
            }
        }



        return true
    }
}
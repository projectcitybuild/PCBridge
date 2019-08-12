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
    override val label: String = "login"
    override val permission: String = "pcbridge.sync.login"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("EnvironmentProvider is null")
        val plugin = environment.plugin ?: throw Exception("Plugin has already been deallocated")

        if (sender !is Player) {
            sender.sendMessage("Console cannot use this command")
            return true
        }
        if (args.size < 2) return false



        return true
    }
}
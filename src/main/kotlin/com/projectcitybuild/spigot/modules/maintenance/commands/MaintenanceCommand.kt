package com.projectcitybuild.spigot.modules.maintenance.commands

import com.projectcitybuild.core.contracts.Commandable
import com.projectcitybuild.core.contracts.Environment
import com.projectcitybuild.entities.models.PluginConfig
import org.bukkit.command.CommandSender

class MaintenanceCommand : Commandable {

    enum class MaintenanceMode {
        ON, OFF
    }

    override var environment: Environment? = null
    override val label: String = "maintenance"

    override fun execute(sender: CommandSender, args: Array<String>, isConsole: Boolean): Boolean {
        val environment = environment ?: throw Exception("Environment missing")
        if (args.size > 1) return false

        val isMaintenanceMode = environment.get(PluginConfig.Settings.MAINTENANCE_MODE()) as? Boolean
                ?: throw Exception("Cannot cast MAINTENANCE_MODE value to Boolean")

        if (args.isEmpty()) {
            if (isMaintenanceMode) {
                sender.sendMessage("Server is currently in maintenance mode")
            } else {
                sender.sendMessage("Server is not in maintenance mode")
            }
            return true
        }

        val newValueInput = args.first().toLowerCase()
        if (!MaintenanceMode.values().map { it.name }.contains(newValueInput)) {
            return false
        }

        val newValue = MaintenanceMode.valueOf(newValueInput)

        when (newValue) {
            MaintenanceMode.ON ->
                if (isMaintenanceMode) {
                    sender.sendMessage("Server is already in maintenance mode")
                } else {
                    environment.set(PluginConfig.Settings.MAINTENANCE_MODE(), true)
                    sender.sendMessage("Server is now in maintenance mode")
                }

            MaintenanceMode.OFF ->
                if (!isMaintenanceMode) {
                    sender.sendMessage("Server is not in maintenance mode")
                } else {
                    environment.set(PluginConfig.Settings.MAINTENANCE_MODE(), false)
                    sender.sendMessage("Server is now open to all players")
                }
        }

        return true
    }
}
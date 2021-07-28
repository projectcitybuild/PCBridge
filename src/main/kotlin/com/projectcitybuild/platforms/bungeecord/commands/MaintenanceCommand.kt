package com.projectcitybuild.platforms.bungeecord.commands

import com.projectcitybuild.core.contracts.ConfigProvider
import com.projectcitybuild.core.entities.CommandResult
import com.projectcitybuild.core.entities.PluginConfig
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommand
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordCommandInput
import com.projectcitybuild.platforms.bungeecord.environment.BungeecordTimer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ProxyServer
import net.md_5.bungee.api.chat.TextComponent
import java.util.concurrent.TimeUnit

class MaintenanceCommand(
        private val config: ConfigProvider,
        private val timer: BungeecordTimer,
        private val proxy: ProxyServer
): BungeecordCommand {

    override val label = "maintenance"
    override val permission = "pcbridge.maintenance.toggle"

    private val timerIdentifier = "maintenance_msg_reminder"

    override fun execute(input: BungeecordCommandInput): CommandResult {
        fun activate() {
            input.player.sendMessage(TextComponent("Maintenance mode has been turned ON").also {
                it.color = ChatColor.AQUA
            })
            config.set(PluginConfig.SETTINGS.MAINTENANCE_MODE, true)

            timer.scheduleRepeating(timerIdentifier, 3, TimeUnit.MINUTES) {
                proxy.players.forEach { player ->
                    player.sendMessage(TextComponent("Reminder: Maintenance mode is currently ON").also {
                        it.color = ChatColor.GRAY
                    })
                }
            }
        }
        fun deactivate() {
            input.player.sendMessage(TextComponent("Maintenance mode has been turned OFF").also {
                it.color = ChatColor.AQUA
            })
            config.set(PluginConfig.SETTINGS.MAINTENANCE_MODE, false)

            timer.cancel(timerIdentifier)
        }

        val isMaintenanceModeOn = config.get(PluginConfig.SETTINGS.MAINTENANCE_MODE)

        if (input.args.isEmpty()) {
            if (isMaintenanceModeOn) {
                deactivate()
            } else {
                activate()
            }
            return CommandResult.EXECUTED
        }

        if (input.args.first().lowercase() == "on") {
            if (isMaintenanceModeOn) {
                input.player.sendMessage(TextComponent("Maintenance mode is already ON").also {
                    it.color = ChatColor.GRAY
                })
            } else {
                activate()
            }
            return CommandResult.EXECUTED
        }
        if (input.args.first().lowercase() == "off") {
            if (!isMaintenanceModeOn) {
                input.player.sendMessage(TextComponent("Maintenance mode is already OFF").also {
                    it.color = ChatColor.GRAY
                })
            } else {
                deactivate()
            }
            return CommandResult.EXECUTED
        }

        input.player.sendMessage(TextComponent("Invalid value. Expected 'on', 'off' or nothing").also {
            it.color = ChatColor.RED
        })
        return CommandResult.INVALID_INPUT
    }
}
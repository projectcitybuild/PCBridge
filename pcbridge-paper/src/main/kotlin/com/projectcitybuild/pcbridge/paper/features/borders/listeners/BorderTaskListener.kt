package com.projectcitybuild.pcbridge.paper.features.borders.listeners

import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import com.projectcitybuild.pcbridge.paper.features.borders.actions.PlayerBorderCheck
import kotlinx.coroutines.runBlocking
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class BorderTaskListener(
    private val plugin: JavaPlugin,
    private val server: Server,
    private val spigotTimer: SpigotTimer,
    private val playerBorderCheck: PlayerBorderCheck
): Listener {
    private var job: Cancellable? = null

    @EventHandler
    fun onPluginEnabled(event: PluginEnableEvent) {
        if (event.plugin != plugin) {
            // PluginEnableEvent is emitted for every plugin, not just ours
            return
        }
        job?.cancel()
        job = spigotTimer.scheduleRepeating(
            identifier = "border_player_check",
            delayTicks = 10,
            repeatingIntervalTicks = 10,
            work = {
                for (player in server.onlinePlayers) {
                    if (job?.isCancelled == true) return@scheduleRepeating

                    runBlocking {
                        playerBorderCheck.check(player)
                    }
                }
            }
        )
    }

    @EventHandler
    fun onPluginDisabled(event: PluginDisableEvent) {
        if (event.plugin != plugin) {
            // PluginEnableEvent is emitted for every plugin, not just ours
            return
        }
        job?.cancel()
        job = null
    }
}
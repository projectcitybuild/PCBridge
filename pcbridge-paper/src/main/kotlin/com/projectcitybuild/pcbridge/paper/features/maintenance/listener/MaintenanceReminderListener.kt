package com.projectcitybuild.pcbridge.paper.features.maintenance.listener

import com.projectcitybuild.pcbridge.paper.architecture.state.Store
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import com.projectcitybuild.pcbridge.paper.features.maintenance.events.MaintenanceToggledEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import java.util.concurrent.TimeUnit

class MaintenanceReminderListener(
    private val store: Store,
    private val server: Server,
    private val timer: SpigotTimer,
) : Listener {
    private var cancellable: Cancellable? = null

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (store.state.maintenance) {
            enable()
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        disable()
    }

    @EventHandler
    fun onMaintenanceToggled(event: MaintenanceToggledEvent) {
        if (event.enabled) {
            enable()
        } else {
            disable()
        }
    }

    private fun enable() {
        cancellable?.cancel()
        cancellable = timer.scheduleRepeating(
            identifier = timerId,
            delay = 5,
            repeatingInterval = 5,
            unit = TimeUnit.MINUTES,
            work = {
                server.broadcast(
                    MiniMessage.miniMessage().deserialize(
                        "<italic><yellow>[Reminder] Maintenance mode is currently ON"
                    )
                )
            }
        )
    }

    private fun disable() {
        cancellable?.cancel()
        cancellable = null
    }

    companion object {
        private const val timerId = "maintenance_reminder"
    }
}

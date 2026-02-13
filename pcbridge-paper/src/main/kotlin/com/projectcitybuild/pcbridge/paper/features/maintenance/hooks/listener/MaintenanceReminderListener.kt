package com.projectcitybuild.pcbridge.paper.features.maintenance.hooks.listener

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.store.Store
import com.projectcitybuild.pcbridge.paper.core.support.spigot.SpigotTimer
import com.projectcitybuild.pcbridge.paper.core.utils.Cancellable
import com.projectcitybuild.pcbridge.paper.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.paper.features.maintenance.maintenanceTracer
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.time.Duration.Companion.minutes

class MaintenanceReminderListener(
    private val plugin: JavaPlugin,
    private val store: Store,
    private val server: Server,
    private val timer: SpigotTimer,
) : Listener {
    private var cancellable: Cancellable? = null

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        // PluginEnableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) {
            return
        }
        event.scopedSync(maintenanceTracer, this::class.java) {
            if (store.state.maintenance) {
                enable()
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        // PluginEnableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) {
            return
        }
        event.scopedSync(maintenanceTracer, this::class.java) {
            disable()
        }
    }

    @EventHandler
    fun onMaintenanceToggled(
        event: MaintenanceToggledEvent,
    ) = event.scopedSync(maintenanceTracer, this::class.java) {
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
            delay = 5.minutes,
            repeatingInterval = 5.minutes,
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

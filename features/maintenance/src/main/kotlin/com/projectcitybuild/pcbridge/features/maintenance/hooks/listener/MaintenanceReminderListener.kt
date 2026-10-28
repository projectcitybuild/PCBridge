package com.projectcitybuild.pcbridge.features.maintenance.hooks.listener

import com.projectcitybuild.pcbridge.core.l10n.l10n
import com.projectcitybuild.pcbridge.core.utils.Cancellable
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.MaintenanceToggledEvent
import com.projectcitybuild.pcbridge.features.maintenance.domain.data.maintenanceStateKey
import com.projectcitybuild.pcbridge.features.maintenance.maintenanceTracer
import com.projectcitybuild.pcbridge.platform.paper.scheduling.SpigotTimer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.state.store.Store
import org.bukkit.Server
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.java
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
        if (event.plugin != plugin) return

        event.tracedSync(maintenanceTracer, this::class.java) {
            if (store.state(maintenanceStateKey).enabled) {
                enable()
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin != plugin) return

        event.tracedSync(maintenanceTracer, this::class.java) {
            disable()
        }
    }

    @EventHandler
    fun onMaintenanceToggled(event: MaintenanceToggledEvent) = event.tracedSync(maintenanceTracer, this::class.java) {
        if (event.enabled) {
            enable()
        } else {
            disable()
        }
    }

    private fun enable() {
        cancellable?.cancel()
        cancellable =
            timer.scheduleRepeating(
                identifier = TIMER_ID,
                delay = 5.minutes,
                repeatingInterval = 5.minutes,
                work = { server.broadcast(l10n.maintenance.maintenanceReminder) },
            )
    }

    private fun disable() {
        cancellable?.cancel()
        cancellable = null
    }

    companion object {
        private const val TIMER_ID = "maintenance:maintenance_reminder"
    }
}

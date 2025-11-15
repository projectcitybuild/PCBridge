package com.projectcitybuild.pcbridge.paper.features.announcements.listeners

import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.log
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin

class AnnouncementEnableListener(
    private val announcementTimer: StartAnnouncementTimer,
    private val plugin: JavaPlugin,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin != plugin) {
            // PluginEnableEvent is emitted for every plugin, not just ours
            return
        }
        announcementTimer.start()
        log.debug { "Announcement timer started" }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin != plugin) {
            // PluginDisableEvent is emitted for every plugin, not just ours
            return
        }
        announcementTimer.stop()
        log.debug { "Announcement timer stopped" }
    }
}

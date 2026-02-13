package com.projectcitybuild.pcbridge.paper.features.announcements.listeners

import com.projectcitybuild.pcbridge.paper.architecture.listeners.scopedSync
import com.projectcitybuild.pcbridge.paper.core.libs.observability.logging.logSync
import com.projectcitybuild.pcbridge.paper.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.paper.features.announcements.announcementsTracer
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.event.server.PluginEnableEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.jvm.java

class AnnouncementEnableListener(
    private val announcementTimer: StartAnnouncementTimer,
    private val plugin: JavaPlugin,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPluginEnable(event: PluginEnableEvent) {
        // PluginEnableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) return

        event.scopedSync(announcementsTracer, this::class.java) {
            announcementTimer.start()
            logSync.debug { "Announcement timer started" }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        // PluginDisableEvent is emitted for every plugin, not just ours
        if (event.plugin != plugin) return

        event.scopedSync(announcementsTracer, this::class.java) {
            announcementTimer.stop()
            logSync.debug { "Announcement timer stopped" }
        }
    }
}

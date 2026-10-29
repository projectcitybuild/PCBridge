package com.projectcitybuild.pcbridge.features.announcements.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.features.announcements.announcementsTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
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
        if (event.plugin != plugin) return

        event.tracedSync(announcementsTracer, this::class.java) {
            announcementTimer.start()
            logSync.debug { "Announcement timer started" }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin != plugin) return

        event.tracedSync(announcementsTracer, this::class.java) {
            announcementTimer.stop()
            logSync.debug { "Announcement timer stopped" }
        }
    }
}

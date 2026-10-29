package com.projectcitybuild.pcbridge.features.announcements.listeners

import com.projectcitybuild.pcbridge.core.observability.logging.logSync
import com.projectcitybuild.pcbridge.features.announcements.actions.StartAnnouncementTimer
import com.projectcitybuild.pcbridge.features.announcements.announcementsTracer
import com.projectcitybuild.pcbridge.runtime.listeners.tracedSync
import com.projectcitybuild.pcbridge.runtime.remoteconfig.RemoteConfigUpdatedEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener

class AnnouncementConfigListener(
    private val announcementTimer: StartAnnouncementTimer,
) : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onRemoteConfigUpdated(event: RemoteConfigUpdatedEvent) =
        event.tracedSync(announcementsTracer, this::class.java) {
            val prev = event.prev?.config
            val next = event.next.config

            if (prev?.announcements == next.announcements) {
                return@tracedSync
            }

            logSync.info { "Announcement config updated. Restarting announcements" }

            // Restart the timer so that the new remote config values are picked up
            announcementTimer.stop()
            announcementTimer.start()
        }
}
